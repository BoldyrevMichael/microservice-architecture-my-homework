package com.boldyrev.billing.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.boldyrev.billing.service.AccountService;
import com.boldyrev.billing.service.InsufficientFundsException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class BillingEventListenerTest {

  @Mock private AccountService accountService;
  @Mock private KafkaTemplate<String, Object> kafkaTemplate;

  @Test
  void orderCreatedIsChargedAndCompletedEventSent() {
    BillingEventListener listener =
        new BillingEventListener(accountService, kafkaTemplate, JsonMapper.builder().build());

    listener.onOrderCreated("{\"orderId\":10,\"userId\":1,\"price\":60.00}");

    verify(accountService).withdraw(1L, new BigDecimal("60.00"));
    ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
    verify(kafkaTemplate).send(eq(Topics.ORDER_PAYMENT), eq("10"), event.capture());
    assertThat(((OrderPayment) event.getValue()).status()).isEqualTo("COMPLETED");
  }

  @Test
  void insufficientFundsProduceFailedEvent() {
    BillingEventListener listener =
        new BillingEventListener(accountService, kafkaTemplate, JsonMapper.builder().build());
    doThrow(new InsufficientFundsException())
        .when(accountService)
        .withdraw(1L, new BigDecimal("100"));

    listener.onOrderCreated("{\"orderId\":11,\"userId\":1,\"price\":100}");

    ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
    verify(kafkaTemplate).send(eq(Topics.ORDER_PAYMENT), eq("11"), event.capture());
    OrderPayment payment = (OrderPayment) event.getValue();
    assertThat(payment.status()).isEqualTo("FAILED");
    assertThat(payment.reason()).isEqualTo("insufficient funds");
  }
}
