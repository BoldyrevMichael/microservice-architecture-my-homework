package com.boldyrev.warehouse.service;

import com.boldyrev.warehouse.dto.ProductResponse;
import com.boldyrev.warehouse.entity.Product;
import com.boldyrev.warehouse.entity.Reservation;
import com.boldyrev.warehouse.repository.ProductRepository;
import com.boldyrev.warehouse.repository.ReservationRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

  private final ProductRepository productRepository;
  private final ReservationRepository reservationRepository;

  @Autowired
  public StockService(
      ProductRepository productRepository, ReservationRepository reservationRepository) {
    this.productRepository = productRepository;
    this.reservationRepository = reservationRepository;
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> list() {
    return productRepository.findAll().stream().map(ProductResponse::from).toList();
  }

  @Transactional(readOnly = true)
  public ProductResponse get(long id) {
    return ProductResponse.from(find(id));
  }

  // резерв под заказ: остаток уменьшается, запись о резерве нужна для отката
  @Transactional
  public void reserve(long orderId, long productId, int quantity) {
    Product product = find(productId);
    if (product.getAvailable() < quantity) {
      throw new OutOfStockException(productId);
    }
    product.setAvailable(product.getAvailable() - quantity);
    reservationRepository.save(new Reservation(orderId, productId, quantity));
  }

  // компенсация: вернуть остаток, если резерв был
  @Transactional
  public void release(long orderId) {
    reservationRepository
        .findById(orderId)
        .ifPresent(
            reservation -> {
              Product product = find(reservation.getProductId());
              product.setAvailable(product.getAvailable() + reservation.getQuantity());
              reservationRepository.delete(reservation);
            });
  }

  private Product find(long id) {
    return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
  }
}
