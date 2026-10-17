package com.boldyrev.delivery.repository;

import com.boldyrev.delivery.entity.CourierReservation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierReservationRepository extends JpaRepository<CourierReservation, Long> {

  boolean existsBySlot(String slot);

  List<CourierReservation> findByUserIdOrderByOrderIdDesc(long userId);
}
