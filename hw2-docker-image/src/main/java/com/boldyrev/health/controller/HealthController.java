package com.boldyrev.health.controller;

import com.boldyrev.health.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  private static final HealthResponse OK = new HealthResponse("OK");

  // оба пути: со слешем на конце и без
  @GetMapping({"/health", "/health/"})
  public HealthResponse health() {
    return OK;
  }
}
