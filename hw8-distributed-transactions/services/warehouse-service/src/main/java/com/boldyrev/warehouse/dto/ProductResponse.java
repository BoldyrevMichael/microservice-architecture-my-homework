package com.boldyrev.warehouse.dto;

import com.boldyrev.warehouse.entity.Product;

public record ProductResponse(long id, String name, int available) {

  public static ProductResponse from(Product product) {
    return new ProductResponse(product.getId(), product.getName(), product.getAvailable());
  }
}
