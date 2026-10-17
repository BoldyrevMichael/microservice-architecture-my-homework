package com.boldyrev.order.service;

import com.boldyrev.order.dto.CreateOrderRequest;
import com.boldyrev.order.dto.OrderResponse;
import com.boldyrev.order.entity.Order;
import com.boldyrev.order.entity.OrderStatus;
import com.boldyrev.order.event.OrderCompleted;
import com.boldyrev.order.event.OrderEvent;
import com.boldyrev.order.event.OrderEventPublisher;
import com.boldyrev.order.idempotency.IdempotencyConflictException;
import com.boldyrev.order.idempotency.IdempotencyKey;
import com.boldyrev.order.idempotency.IdempotencyKeyId;
import com.boldyrev.order.idempotency.IdempotencyKeyRepository;
import com.boldyrev.order.idempotency.RequestHash;
import com.boldyrev.order.repository.OrderRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final IdempotencyKeyRepository idempotencyKeyRepository;
  private final OrderEventPublisher eventPublisher;

  @Autowired
  public OrderService(
      OrderRepository orderRepository,
      IdempotencyKeyRepository idempotencyKeyRepository,
      OrderEventPublisher eventPublisher) {
    this.orderRepository = orderRepository;
    this.idempotencyKeyRepository = idempotencyKeyRepository;
    this.eventPublisher = eventPublisher;
  }

  // повтор с тем же ключом возвращает уже созданный заказ и не запускает сагу второй раз;
  // тот же ключ с другим телом — ошибка
  @Transactional
  public OrderResponse create(long userId, String idempotencyKey, CreateOrderRequest request) {
    String requestHash = RequestHash.of(request);
    Optional<IdempotencyKey> existing =
        idempotencyKeyRepository.findById(new IdempotencyKeyId(userId, idempotencyKey));
    if (existing.isPresent()) {
      if (!existing.get().getRequestHash().equals(requestHash)) {
        throw new IdempotencyConflictException(idempotencyKey);
      }
      return orderRepository
          .findByIdAndUserId(existing.get().getOrderId(), userId)
          .map(OrderResponse::from)
          .orElseThrow(() -> new OrderNotFoundException(existing.get().getOrderId()));
    }
    Order order =
        orderRepository.save(
            new Order(
                userId, request.price(), request.productId(), request.quantity(), request.slot()));
    idempotencyKeyRepository.save(
        new IdempotencyKey(userId, idempotencyKey, requestHash, order.getId()));
    eventPublisher.orderCreated(
        new OrderEvent(
            order.getId(),
            userId,
            order.getPrice(),
            order.getProductId(),
            order.getQuantity(),
            order.getSlot(),
            "CREATED",
            null));
    return OrderResponse.from(order);
  }

  @Transactional(readOnly = true)
  public List<OrderResponse> list(long userId) {
    return orderRepository.findByUserIdOrderByIdDesc(userId).stream()
        .map(OrderResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public OrderResponse get(long userId, long id) {
    return orderRepository
        .findByIdAndUserId(id, userId)
        .map(OrderResponse::from)
        .orElseThrow(() -> new OrderNotFoundException(id));
  }

  // успешный шаг продвигает заказ, отказ любого шага отменяет его; финал уходит уведомлениям
  @Transactional
  public void applyStep(OrderEvent event, OrderStatus onSuccess) {
    Order order =
        orderRepository
            .findById(event.orderId())
            .orElseThrow(() -> new OrderNotFoundException(event.orderId()));
    if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.CONFIRMED) {
      return;
    }
    if (event.failed()) {
      order.setStatus(OrderStatus.CANCELLED);
      order.setReason(event.reason());
    } else {
      order.setStatus(onSuccess);
    }
    if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.CONFIRMED) {
      eventPublisher.orderCompleted(
          new OrderCompleted(
              order.getId(),
              order.getUserId(),
              order.getPrice(),
              order.getStatus().name(),
              order.getReason()));
    }
  }
}
