package com.boldyrev.health.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

  @Autowired private MockMvcTester mvc;

  @ParameterizedTest
  @ValueSource(strings = {"/health/", "/health"})
  void returnsOkStatus(String path) {
    assertThat(mvc.get().uri(path))
        .hasStatus(HttpStatus.OK)
        .hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON)
        .bodyJson()
        .isEqualTo("{\"status\":\"OK\"}");
  }

  @Test
  void otherPathIsNotFound() {
    assertThat(mvc.get().uri("/healthz")).hasStatus(HttpStatus.NOT_FOUND);
  }
}
