package com.boldyrev.warehouse.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.boldyrev.warehouse.service.StockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class WarehouseEventListenerTest {

  private static final String ORDER =
      "{\"orderId\":10,\"userId\":1,\"price\":60,\"productId\":1,\"quantity\":2,"
          + "\"slot\":\"2026-10-20T10:00\",\"status\":\"%s\",\"reason\":null}";

  @Mock private StockService stockService;
  @Mock private KafkaTemplate<String, Object> kafkaTemplate;

  @Test
  void completedPaymentReservesStock() {
    listener().onPayment(ORDER.formatted("COMPLETED"));

    verify(stockService).reserve(10L, 1L, 2);
    ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
    verify(kafkaTemplate).send(eq(Topics.ORDER_STOCK), eq("10"), event.capture());
    assertThat(((OrderEvent) event.getValue()).status()).isEqualTo("RESERVED");
  }

  @Test
  void failedPaymentIsIgnored() {
    listener().onPayment(ORDER.formatted("FAILED"));

    verify(stockService, never()).reserve(any(Long.class), any(Long.class), any(Integer.class));
    verify(kafkaTemplate, never()).send(any(), any(), any());
  }

  @Test
  void failedDeliveryReleasesReservation() {
    listener().onDelivery(ORDER.formatted("FAILED"));

    verify(stockService).release(10L);
  }

  private WarehouseEventListener listener() {
    return new WarehouseEventListener(stockService, kafkaTemplate, JsonMapper.builder().build());
  }
}
