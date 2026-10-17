package com.boldyrev.user.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.boldyrev.user.config.WebConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(HealthController.class)
@Import(WebConfig.class)
class HealthControllerTest {

  @Autowired private MockMvcTester mvc;

  @ParameterizedTest
  @ValueSource(strings = {"/health", "/health/"})
  void returnsOkStatus(String path) {
    assertThat(mvc.get().uri(path))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .isEqualTo("{\"status\":\"OK\"}");
  }
}
