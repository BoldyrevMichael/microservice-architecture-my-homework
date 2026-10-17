package com.boldyrev.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boldyrev.order.dto.OrderResponse;
import com.boldyrev.order.entity.Order;
import com.boldyrev.order.entity.OrderStatus;
import com.boldyrev.order.event.OrderCreated;
import com.boldyrev.order.event.OrderEventPublisher;
import com.boldyrev.order.event.OrderPayment;
import com.boldyrev.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;
  @Mock private OrderEventPublisher eventPublisher;
  @InjectMocks private OrderService orderService;

  @Test
  void createStoresNewOrderAndPublishesEvent() {
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            i -> {
              Order o = i.getArgument(0);
              o.setId(10L);
              return o;
            });

    OrderResponse created = orderService.create(1L, new BigDecimal("60"));

    assertThat(created.status()).isEqualTo("NEW");
    verify(eventPublisher).orderCreated(new OrderCreated(10L, 1L, new BigDecimal("60")));
  }

  @Test
  void completedPaymentMarksOrderPaid() {
    Order order = new Order(1L, new BigDecimal("60"));
    when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

    orderService.applyPayment(new OrderPayment(10L, 1L, new BigDecimal("60"), "COMPLETED", null));

    assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
  }

  @Test
  void failedPaymentMarksOrderRejectedWithReason() {
    Order order = new Order(1L, new BigDecimal("100"));
    when(orderRepository.findById(11L)).thenReturn(Optional.of(order));

    orderService.applyPayment(
        new OrderPayment(11L, 1L, new BigDecimal("100"), "FAILED", "insufficient funds"));

    assertThat(order.getStatus()).isEqualTo(OrderStatus.REJECTED);
    assertThat(order.getReason()).isEqualTo("insufficient funds");
  }

  @Test
  void foreignOrderIsNotFound() {
    when(orderRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.get(2L, 10L)).isInstanceOf(OrderNotFoundException.class);
  }
}
