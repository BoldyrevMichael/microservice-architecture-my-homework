package com.boldyrev.order.controller;

import com.boldyrev.order.dto.CreateOrderRequest;
import com.boldyrev.order.dto.OrderResponse;
import com.boldyrev.order.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// заказы только свои: идентификатор пользователя подставляет шлюз в X-User-Id
@RestController
@RequestMapping("/orders")
public class OrderController {

  private final OrderService orderService;

  @Autowired
  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrderResponse create(
      @RequestHeader("X-User-Id") long userId, @Valid @RequestBody CreateOrderRequest request) {
    return orderService.create(userId, request.price());
  }

  @GetMapping
  public List<OrderResponse> list(@RequestHeader("X-User-Id") long userId) {
    return orderService.list(userId);
  }

  @GetMapping("/{id}")
  public OrderResponse get(@RequestHeader("X-User-Id") long userId, @PathVariable long id) {
    return orderService.get(userId, id);
  }
}
