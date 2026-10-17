package com.boldyrev.delivery.service;

public class SlotTakenException extends RuntimeException {

  public SlotTakenException(String slot) {
    super("no courier available at " + slot);
  }
}
