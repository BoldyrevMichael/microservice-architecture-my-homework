package com.boldyrev.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  private String email;

  @Column(nullable = false)
  private String subject;

  @Column(nullable = false)
  private String body;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  public Message(long userId, String email, String subject, String body) {
    this.userId = userId;
    this.email = email;
    this.subject = subject;
    this.body = body;
  }
}
