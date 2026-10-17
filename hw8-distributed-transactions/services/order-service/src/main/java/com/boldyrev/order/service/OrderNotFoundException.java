package com.boldyrev.order.service;

public class OrderNotFoundException extends RuntimeException {

  public OrderNotFoundException(long id) {
    super("order " + id + " not found");
  }
}
