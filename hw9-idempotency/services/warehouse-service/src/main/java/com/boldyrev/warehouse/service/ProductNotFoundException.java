package com.boldyrev.warehouse.service;

public class ProductNotFoundException extends RuntimeException {

  public ProductNotFoundException(long id) {
    super("product " + id + " not found");
  }
}
