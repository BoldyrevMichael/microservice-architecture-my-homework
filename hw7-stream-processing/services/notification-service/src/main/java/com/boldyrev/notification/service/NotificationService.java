package com.boldyrev.notification.service;

import com.boldyrev.notification.dto.MessageResponse;
import com.boldyrev.notification.entity.Message;
import com.boldyrev.notification.entity.Recipient;
import com.boldyrev.notification.repository.MessageRepository;
import com.boldyrev.notification.repository.RecipientRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

  private final RecipientRepository recipientRepository;
  private final MessageRepository messageRepository;

  @Autowired
  public NotificationService(
      RecipientRepository recipientRepository, MessageRepository messageRepository) {
    this.recipientRepository = recipientRepository;
    this.messageRepository = messageRepository;
  }

  @Transactional
  public void rememberRecipient(long userId, String email) {
    recipientRepository.save(new Recipient(userId, email));
  }

  // «письмо счастья» или «письмо горя»: отправка — это запись в таблицу
  @Transactional
  public void orderPaymentResult(
      long userId, long orderId, BigDecimal price, boolean paid, String reason) {
    String email = recipientRepository.findById(userId).map(Recipient::getEmail).orElse(null);
    String subject = paid ? "Заказ " + orderId + " оплачен" : "Заказ " + orderId + " отклонён";
    String body =
        paid
            ? "С вашего счёта списано " + price + ". Спасибо за покупку!"
            : "Оплатить заказ на сумму " + price + " не удалось: " + reason + ".";
    messageRepository.save(new Message(userId, email, subject, body));
  }

  @Transactional(readOnly = true)
  public List<MessageResponse> list(long userId) {
    return messageRepository.findByUserIdOrderByIdDesc(userId).stream()
        .map(MessageResponse::from)
        .toList();
  }
}
