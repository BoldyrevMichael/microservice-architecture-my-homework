package com.boldyrev.billing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {

  @Id private Long userId;

  @Column(nullable = false)
  private BigDecimal balance = BigDecimal.ZERO;

  public Account(long userId) {
    this.userId = userId;
  }
}
