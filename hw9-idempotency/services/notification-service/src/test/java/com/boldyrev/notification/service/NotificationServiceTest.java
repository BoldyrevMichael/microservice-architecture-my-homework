package com.boldyrev.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boldyrev.notification.entity.Message;
import com.boldyrev.notification.entity.Recipient;
import com.boldyrev.notification.repository.MessageRepository;
import com.boldyrev.notification.repository.RecipientRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private RecipientRepository recipientRepository;
  @Mock private MessageRepository messageRepository;
  @InjectMocks private NotificationService notificationService;

  @Test
  void confirmedOrderProducesHappyLetterToKnownEmail() {
    when(recipientRepository.findById(1L)).thenReturn(Optional.of(new Recipient(1L, "a@b.c")));

    notificationService.orderResult(1L, 10L, new BigDecimal("60"), true, null);

    Message message = savedMessage();
    assertThat(message.getEmail()).isEqualTo("a@b.c");
    assertThat(message.getSubject()).isEqualTo("Заказ 10 подтверждён");
    assertThat(message.getBody()).contains("60");
  }

  @Test
  void cancelledOrderProducesSadLetterWithReason() {
    when(recipientRepository.findById(1L)).thenReturn(Optional.empty());

    notificationService.orderResult(1L, 11L, new BigDecimal("100"), false, "insufficient funds");

    Message message = savedMessage();
    assertThat(message.getEmail()).isNull();
    assertThat(message.getSubject()).isEqualTo("Заказ 11 отменён");
    assertThat(message.getBody()).contains("insufficient funds");
  }

  private Message savedMessage() {
    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(messageRepository).save(captor.capture());
    return captor.getValue();
  }
}
