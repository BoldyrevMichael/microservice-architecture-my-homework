package com.boldyrev.user.controller;

import com.boldyrev.user.dto.LoginRequest;
import com.boldyrev.user.dto.RegisterRequest;
import com.boldyrev.user.dto.TokenResponse;
import com.boldyrev.user.dto.UserResponse;
import com.boldyrev.user.security.GatewayUserFilter;
import com.boldyrev.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  private static final String BEARER = "Bearer ";

  private final AuthService authService;

  @Autowired
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  public TokenResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  // вызывается ingress-nginx (auth-url) на каждый запрос к защищённым путям
  @GetMapping("/auth/verify")
  public ResponseEntity<Void> verify(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
    if (authorization == null || !authorization.startsWith(BEARER)) {
      throw new BadCredentialsException("missing bearer token");
    }
    long userId = authService.verify(authorization.substring(BEARER.length()));
    return ResponseEntity.noContent()
        .header(GatewayUserFilter.USER_ID_HEADER, String.valueOf(userId))
        .build();
  }
}
