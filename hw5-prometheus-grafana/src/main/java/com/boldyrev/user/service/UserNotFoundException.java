package com.boldyrev.user.service;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(long id) {
    super("user " + id + " not found");
  }
}
