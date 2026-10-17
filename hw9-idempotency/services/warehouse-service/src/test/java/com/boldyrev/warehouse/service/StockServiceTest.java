package com.boldyrev.warehouse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boldyrev.warehouse.entity.Product;
import com.boldyrev.warehouse.entity.Reservation;
import com.boldyrev.warehouse.repository.ProductRepository;
import com.boldyrev.warehouse.repository.ReservationRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

  @Mock private ProductRepository productRepository;
  @Mock private ReservationRepository reservationRepository;
  @InjectMocks private StockService stockService;

  @Test
  void reserveDecreasesStockAndRemembersReservation() {
    Product product = new Product(1L, "BLT", 10);
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    stockService.reserve(10L, 1L, 3);

    assertThat(product.getAvailable()).isEqualTo(7);
    verify(reservationRepository).save(any(Reservation.class));
  }

  @Test
  void reserveMoreThanAvailableFailsWithoutChanges() {
    Product product = new Product(2L, "Lemonade", 0);
    when(productRepository.findById(2L)).thenReturn(Optional.of(product));

    assertThatThrownBy(() -> stockService.reserve(10L, 2L, 1))
        .isInstanceOf(OutOfStockException.class);
    assertThat(product.getAvailable()).isZero();
    verify(reservationRepository, never()).save(any());
  }

  @Test
  void releaseReturnsReservedQuantity() {
    Product product = new Product(1L, "BLT", 7);
    when(reservationRepository.findById(10L)).thenReturn(Optional.of(new Reservation(10L, 1L, 3)));
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    stockService.release(10L);

    assertThat(product.getAvailable()).isEqualTo(10);
    verify(reservationRepository).delete(any(Reservation.class));
  }

  @Test
  void releaseWithoutReservationIsNoop() {
    when(reservationRepository.findById(10L)).thenReturn(Optional.empty());

    stockService.release(10L);

    verify(productRepository, never()).findById(any());
  }
}
