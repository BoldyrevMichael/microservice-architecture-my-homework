package com.boldyrev.notification.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recipients")
@Getter
@Setter
@NoArgsConstructor
public class Recipient {

  @Id private Long userId;

  private String email;

  public Recipient(long userId, String email) {
    this.userId = userId;
    this.email = email;
  }
}
