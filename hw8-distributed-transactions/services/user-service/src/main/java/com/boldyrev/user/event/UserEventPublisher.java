package com.boldyrev.user.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {

  public static final String TOPIC = "user.registered";

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Autowired
  public UserEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void userRegistered(UserRegistered event) {
    kafkaTemplate.send(TOPIC, String.valueOf(event.userId()), event);
  }
}
