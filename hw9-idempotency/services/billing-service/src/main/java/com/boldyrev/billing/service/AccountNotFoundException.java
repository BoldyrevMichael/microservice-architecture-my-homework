package com.boldyrev.billing.service;

public class AccountNotFoundException extends RuntimeException {

  public AccountNotFoundException(long userId) {
    super("account of user " + userId + " not found");
  }
}
