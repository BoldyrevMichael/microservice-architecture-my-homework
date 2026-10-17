package com.boldyrev.delivery.dto;

import com.boldyrev.delivery.entity.CourierReservation;

public record ReservationResponse(long orderId, String slot) {

  public static ReservationResponse from(CourierReservation reservation) {
    return new ReservationResponse(reservation.getOrderId(), reservation.getSlot());
  }
}
