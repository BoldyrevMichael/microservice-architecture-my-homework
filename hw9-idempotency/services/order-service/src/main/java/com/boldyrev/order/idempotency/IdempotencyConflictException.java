package com.boldyrev.order.idempotency;

// тот же ключ, но другое тело запроса
public class IdempotencyConflictException extends RuntimeException {

  public IdempotencyConflictException(String key) {
    super("idempotency key " + key + " was already used with a different request");
  }
}
