package com.boldyrev.order.entity;

// шаги саги: NEW → PAID → RESERVED → CONFIRMED; любой отказ — CANCELLED с причиной
public enum OrderStatus {
  NEW,
  PAID,
  RESERVED,
  CONFIRMED,
  CANCELLED
}
