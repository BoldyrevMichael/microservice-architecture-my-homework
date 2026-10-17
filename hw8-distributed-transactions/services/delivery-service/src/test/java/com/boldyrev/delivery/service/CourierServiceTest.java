package com.boldyrev.delivery.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boldyrev.delivery.entity.CourierReservation;
import com.boldyrev.delivery.repository.CourierReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourierServiceTest {

  @Mock private CourierReservationRepository reservationRepository;
  @InjectMocks private CourierService courierService;

  @Test
  void freeSlotIsReserved() {
    when(reservationRepository.existsBySlot("2026-10-20T10:00")).thenReturn(false);

    courierService.reserve(10L, 1L, "2026-10-20T10:00");

    verify(reservationRepository).save(any(CourierReservation.class));
  }

  @Test
  void takenSlotIsRejected() {
    when(reservationRepository.existsBySlot("2026-10-20T10:00")).thenReturn(true);

    assertThatThrownBy(() -> courierService.reserve(11L, 1L, "2026-10-20T10:00"))
        .isInstanceOf(SlotTakenException.class)
        .hasMessageContaining("2026-10-20T10:00");
    verify(reservationRepository, never()).save(any());
  }
}
