package com.boldyrev.warehouse.repository;

import com.boldyrev.warehouse.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {}
