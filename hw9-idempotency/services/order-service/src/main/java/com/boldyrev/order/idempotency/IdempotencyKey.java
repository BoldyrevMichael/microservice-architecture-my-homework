package com.boldyrev.order.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "idempotency_keys")
@Getter
@NoArgsConstructor
public class IdempotencyKey {

  @EmbeddedId private IdempotencyKeyId id;

  @Column(nullable = false)
  private String requestHash;

  @Column(nullable = false)
  private Long orderId;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  public IdempotencyKey(long userId, String key, String requestHash, long orderId) {
    this.id = new IdempotencyKeyId(userId, key);
    this.requestHash = requestHash;
    this.orderId = orderId;
  }
}
