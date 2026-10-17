package com.boldyrev.billing.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.boldyrev.billing.service.AccountService;
import com.boldyrev.billing.service.InsufficientFundsException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class BillingEventListenerTest {

  private static final String ORDER =
      "{\"orderId\":10,\"userId\":1,\"price\":60.00,\"productId\":1,\"quantity\":1,"
          + "\"slot\":\"2026-10-20T10:00\",\"status\":\"%s\",\"reason\":%s}";

  @Mock private AccountService accountService;
  @Mock private KafkaTemplate<String, Object> kafkaTemplate;
  private BillingEventListener listener;

  @BeforeEach
  void setUp() {
    listener =
        new BillingEventListener(accountService, kafkaTemplate, JsonMapper.builder().build());
  }

  @Test
  void orderCreatedIsChargedAndCompletedEventSent() {
    listener.onOrderCreated(ORDER.formatted("CREATED", "null"));

    verify(accountService).withdraw(1L, new BigDecimal("60.00"));
    assertThat(sentPayment().status()).isEqualTo("COMPLETED");
  }

  @Test
  void insufficientFundsProduceFailedEvent() {
    doThrow(new InsufficientFundsException())
        .when(accountService)
        .withdraw(1L, new BigDecimal("60.00"));

    listener.onOrderCreated(ORDER.formatted("CREATED", "null"));

    OrderEvent payment = sentPayment();
    assertThat(payment.status()).isEqualTo("FAILED");
    assertThat(payment.reason()).isEqualTo("insufficient funds");
  }

  @Test
  void failedLaterStepRefundsMoney() {
    listener.onLaterStepFailed(ORDER.formatted("FAILED", "\"out of stock\""));

    verify(accountService).deposit(1L, new BigDecimal("60.00"));
  }

  @Test
  void successfulLaterStepDoesNotTouchAccount() {
    listener.onLaterStepFailed(ORDER.formatted("RESERVED", "null"));

    verify(accountService, never()).deposit(any(Long.class), any());
  }

  private OrderEvent sentPayment() {
    ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
    verify(kafkaTemplate).send(eq(Topics.ORDER_PAYMENT), eq("10"), event.capture());
    return (OrderEvent) event.getValue();
  }
}
