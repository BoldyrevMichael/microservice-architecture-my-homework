package com.boldyrev.user.controller;

import com.boldyrev.user.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  private static final HealthResponse OK = new HealthResponse("OK");

  @GetMapping("/health")
  public HealthResponse health() {
    return OK;
  }
}
