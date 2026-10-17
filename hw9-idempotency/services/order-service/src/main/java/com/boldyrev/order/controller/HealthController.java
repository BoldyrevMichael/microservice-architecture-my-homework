package com.boldyrev.order.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  private static final Map<String, String> OK = Map.of("status", "OK");

  @GetMapping("/health")
  public Map<String, String> health() {
    return OK;
  }
}
