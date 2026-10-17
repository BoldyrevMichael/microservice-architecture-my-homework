package com.boldyrev.delivery.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "courier_reservations")
@Getter
@Setter
@NoArgsConstructor
public class CourierReservation {

  @Id private Long orderId;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false, unique = true)
  private String slot;

  public CourierReservation(long orderId, long userId, String slot) {
    this.orderId = orderId;
    this.userId = userId;
    this.slot = slot;
  }
}
