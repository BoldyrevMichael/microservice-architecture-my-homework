package com.boldyrev.order.service;

import com.boldyrev.order.dto.OrderResponse;
import com.boldyrev.order.entity.Order;
import com.boldyrev.order.entity.OrderStatus;
import com.boldyrev.order.event.OrderCreated;
import com.boldyrev.order.event.OrderEventPublisher;
import com.boldyrev.order.event.OrderPayment;
import com.boldyrev.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final OrderEventPublisher eventPublisher;

  @Autowired
  public OrderService(OrderRepository orderRepository, OrderEventPublisher eventPublisher) {
    this.orderRepository = orderRepository;
    this.eventPublisher = eventPublisher;
  }

  // заказ сохраняется в статусе NEW, дальше его судьбу решает событие от биллинга
  @Transactional
  public OrderResponse create(long userId, BigDecimal price) {
    Order order = orderRepository.save(new Order(userId, price));
    eventPublisher.orderCreated(new OrderCreated(order.getId(), userId, price));
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

  @Transactional
  public void applyPayment(OrderPayment payment) {
    Order order =
        orderRepository
            .findById(payment.orderId())
            .orElseThrow(() -> new OrderNotFoundException(payment.orderId()));
    order.setStatus(payment.completed() ? OrderStatus.PAID : OrderStatus.REJECTED);
    order.setReason(payment.reason());
  }
}
