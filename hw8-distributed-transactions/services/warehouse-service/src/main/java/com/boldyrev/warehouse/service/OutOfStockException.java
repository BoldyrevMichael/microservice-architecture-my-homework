package com.boldyrev.warehouse.service;

public class OutOfStockException extends RuntimeException {

  public OutOfStockException(long productId) {
    super("product " + productId + " is out of stock");
  }
}
