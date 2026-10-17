package com.boldyrev.delivery.controller;

import com.boldyrev.delivery.dto.ReservationResponse;
import com.boldyrev.delivery.service.CourierService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

// резервы курьера только свои: идентификатор пользователя подставляет шлюз в X-User-Id
@RestController
public class ReservationController {

  private final CourierService courierService;

  @Autowired
  public ReservationController(CourierService courierService) {
    this.courierService = courierService;
  }

  @GetMapping("/delivery/reservations")
  public List<ReservationResponse> list(@RequestHeader("X-User-Id") long userId) {
    return courierService.list(userId);
  }
}
