package com.boldyrev.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.boldyrev.user.config.WebConfig;
import com.boldyrev.user.dto.LoginRequest;
import com.boldyrev.user.dto.RegisterRequest;
import com.boldyrev.user.dto.TokenResponse;
import com.boldyrev.user.dto.UserResponse;
import com.boldyrev.user.security.SecurityConfig;
import com.boldyrev.user.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, WebConfig.class})
class AuthControllerTest {

  @Autowired private MockMvcTester mvc;
  @MockitoBean private AuthService authService;

  @Test
  void registerIsOpenAndReturnsCreated() {
    when(authService.register(any(RegisterRequest.class)))
        .thenReturn(new UserResponse(1L, "johndoe", "John", "Doe", "j@d.com", "+7100"));

    assertThat(
            mvc.post()
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"johndoe\",\"password\":\"secret123\"}"))
        .hasStatus(HttpStatus.CREATED)
        .bodyJson()
        .extractingPath("$.id")
        .isEqualTo(1);
  }

  @Test
  void loginReturnsToken() {
    when(authService.login(any(LoginRequest.class))).thenReturn(new TokenResponse("jwt", 3600));

    assertThat(
            mvc.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"johndoe\",\"password\":\"secret123\"}"))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .extractingPath("$.token")
        .isEqualTo("jwt");
  }

  @Test
  void verifyWithoutTokenIsUnauthorized() {
    assertThat(mvc.get().uri("/auth/verify")).hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void verifyWithBadTokenIsUnauthorized() {
    when(authService.verify("bad")).thenThrow(new BadCredentialsException("invalid token"));

    assertThat(mvc.get().uri("/auth/verify").header("Authorization", "Bearer bad"))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void verifyReturnsUserIdHeader() {
    when(authService.verify("good")).thenReturn(7L);

    assertThat(mvc.get().uri("/auth/verify").header("Authorization", "Bearer good"))
        .hasStatus(HttpStatus.NO_CONTENT)
        .hasHeader("X-User-Id", "7");
  }
}
