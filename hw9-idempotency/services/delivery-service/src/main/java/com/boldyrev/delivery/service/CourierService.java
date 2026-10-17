package com.boldyrev.delivery.service;

import com.boldyrev.delivery.dto.ReservationResponse;
import com.boldyrev.delivery.entity.CourierReservation;
import com.boldyrev.delivery.repository.CourierReservationRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourierService {

  private final CourierReservationRepository reservationRepository;

  @Autowired
  public CourierService(CourierReservationRepository reservationRepository) {
    this.reservationRepository = reservationRepository;
  }

  // один курьер: слот занят, если на него уже есть резерв
  @Transactional
  public void reserve(long orderId, long userId, String slot) {
    if (reservationRepository.existsBySlot(slot)) {
      throw new SlotTakenException(slot);
    }
    reservationRepository.save(new CourierReservation(orderId, userId, slot));
  }

  @Transactional(readOnly = true)
  public List<ReservationResponse> list(long userId) {
    return reservationRepository.findByUserIdOrderByOrderIdDesc(userId).stream()
        .map(ReservationResponse::from)
        .toList();
  }
}
