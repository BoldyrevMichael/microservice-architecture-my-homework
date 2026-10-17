package com.boldyrev.billing.service;

public class InsufficientFundsException extends RuntimeException {

  public InsufficientFundsException() {
    super("insufficient funds");
  }
}
