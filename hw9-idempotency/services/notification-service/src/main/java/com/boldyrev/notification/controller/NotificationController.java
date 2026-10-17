package com.boldyrev.notification.controller;

import com.boldyrev.notification.dto.MessageResponse;
import com.boldyrev.notification.service.NotificationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

// письма только свои: идентификатор пользователя подставляет шлюз в X-User-Id
@RestController
public class NotificationController {

  private final NotificationService notificationService;

  @Autowired
  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping("/notifications")
  public List<MessageResponse> list(@RequestHeader("X-User-Id") long userId) {
    return notificationService.list(userId);
  }
}
