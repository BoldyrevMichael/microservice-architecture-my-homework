package com.boldyrev.order.entity;

// NEW — ждём итог оплаты; PAID — деньги списаны; REJECTED — биллинг отказал
public enum OrderStatus {
  NEW,
  PAID,
  REJECTED
}
