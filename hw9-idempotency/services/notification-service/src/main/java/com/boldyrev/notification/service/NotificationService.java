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

  // «письмо счастья» или «письмо горя» по итогу саги: отправка — это запись в таблицу
  @Transactional
  public void orderResult(
      long userId, long orderId, BigDecimal price, boolean confirmed, String reason) {
    String email = recipientRepository.findById(userId).map(Recipient::getEmail).orElse(null);
    String subject =
        confirmed ? "Заказ " + orderId + " подтверждён" : "Заказ " + orderId + " отменён";
    String body =
        confirmed
            ? "Оплата " + price + " прошла, товар зарезервирован, курьер назначен. Спасибо!"
            : "Заказ на сумму " + price + " отменён: " + reason + ". Деньги возвращены на счёт.";
    messageRepository.save(new Message(userId, email, subject, body));
  }

  @Transactional(readOnly = true)
  public List<MessageResponse> list(long userId) {
    return messageRepository.findByUserIdOrderByIdDesc(userId).stream()
        .map(MessageResponse::from)
        .toList();
  }
}
