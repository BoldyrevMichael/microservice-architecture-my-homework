package com.boldyrev.notification.dto;

import com.boldyrev.notification.entity.Message;
import java.time.Instant;

public record MessageResponse(
    long id, long userId, String email, String subject, String body, Instant createdAt) {

  public static MessageResponse from(Message message) {
    return new MessageResponse(
        message.getId(),
        message.getUserId(),
        message.getEmail(),
        message.getSubject(),
        message.getBody(),
        message.getCreatedAt());
  }
}
