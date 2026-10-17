package com.boldyrev.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boldyrev.order.dto.CreateOrderRequest;
import com.boldyrev.order.entity.Order;
import com.boldyrev.order.entity.OrderStatus;
import com.boldyrev.order.event.OrderCompleted;
import com.boldyrev.order.event.OrderEvent;
import com.boldyrev.order.event.OrderEventPublisher;
import com.boldyrev.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;
  @Mock private OrderEventPublisher eventPublisher;
  @InjectMocks private OrderService orderService;

  @Test
  void createStartsSagaWithOrderSnapshot() {
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            i -> {
              Order o = i.getArgument(0);
              o.setId(10L);
              return o;
            });

    orderService.create(
        1L, new CreateOrderRequest(new BigDecimal("60"), 1L, 2, "2026-10-20T10:00"));

    ArgumentCaptor<OrderEvent> event = ArgumentCaptor.forClass(OrderEvent.class);
    verify(eventPublisher).orderCreated(event.capture());
    assertThat(event.getValue().orderId()).isEqualTo(10L);
    assertThat(event.getValue().quantity()).isEqualTo(2);
    assertThat(event.getValue().slot()).isEqualTo("2026-10-20T10:00");
  }

  @Test
  void successfulStepsAdvanceStatusAndConfirmAtTheEnd() {
    Order order = order();
    when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

    orderService.applyStep(event("COMPLETED", null), OrderStatus.PAID);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    verify(eventPublisher, never()).orderCompleted(any());

    orderService.applyStep(event("RESERVED", null), OrderStatus.RESERVED);
    orderService.applyStep(event("RESERVED", null), OrderStatus.CONFIRMED);

    assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    ArgumentCaptor<OrderCompleted> done = ArgumentCaptor.forClass(OrderCompleted.class);
    verify(eventPublisher).orderCompleted(done.capture());
    assertThat(done.getValue().status()).isEqualTo("CONFIRMED");
  }

  @Test
  void failedStepCancelsOrderWithReason() {
    Order order = order();
    order.setStatus(OrderStatus.PAID);
    when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

    orderService.applyStep(event("FAILED", "out of stock"), OrderStatus.RESERVED);

    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    assertThat(order.getReason()).isEqualTo("out of stock");
    ArgumentCaptor<OrderCompleted> done = ArgumentCaptor.forClass(OrderCompleted.class);
    verify(eventPublisher).orderCompleted(done.capture());
    assertThat(done.getValue().status()).isEqualTo("CANCELLED");
  }

  @Test
  void stepsAfterCancellationAreIgnored() {
    Order order = order();
    order.setStatus(OrderStatus.CANCELLED);
    when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

    orderService.applyStep(event("RESERVED", null), OrderStatus.CONFIRMED);

    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    verify(eventPublisher, never()).orderCompleted(any());
  }

  private static Order order() {
    Order order = new Order(1L, new BigDecimal("60"), 1L, 1, "2026-10-20T10:00");
    order.setId(10L);
    return order;
  }

  private static OrderEvent event(String status, String reason) {
    return new OrderEvent(10L, 1L, new BigDecimal("60"), 1L, 1, "2026-10-20T10:00", status, reason);
  }
}
