package com.boldyrev.user.service;

import com.boldyrev.user.dto.LoginRequest;
import com.boldyrev.user.dto.RegisterRequest;
import com.boldyrev.user.dto.TokenResponse;
import com.boldyrev.user.dto.UserResponse;
import com.boldyrev.user.entity.User;
import com.boldyrev.user.event.UserEventPublisher;
import com.boldyrev.user.event.UserRegistered;
import com.boldyrev.user.repository.UserRepository;
import com.boldyrev.user.security.JwtProperties;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final JwtProperties jwtProperties;
  private final UserEventPublisher eventPublisher;

  @Autowired
  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtEncoder jwtEncoder,
      JwtDecoder jwtDecoder,
      JwtProperties jwtProperties,
      UserEventPublisher eventPublisher) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtEncoder = jwtEncoder;
    this.jwtDecoder = jwtDecoder;
    this.jwtProperties = jwtProperties;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public UserResponse register(RegisterRequest request) {
    User user = new User();
    user.setUsername(request.username());
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setEmail(request.email());
    user.setPhone(request.phone());
    User saved = userRepository.save(user);
    // по условию задания сбои не рассматриваем: событие уходит сразу после сохранения
    eventPublisher.userRegistered(
        new UserRegistered(saved.getId(), saved.getUsername(), saved.getEmail()));
    return UserResponse.from(saved);
  }

  @Transactional(readOnly = true)
  public TokenResponse login(LoginRequest request) {
    User user =
        userRepository
            .findByUsername(request.username())
            .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
            .orElseThrow(() -> new BadCredentialsException("bad credentials"));
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .subject(String.valueOf(user.getId()))
            .claim("username", user.getUsername())
            .issuedAt(now)
            .expiresAt(now.plus(jwtProperties.ttl()))
            .build();
    String token =
        jwtEncoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
    return new TokenResponse(token, jwtProperties.ttl().toSeconds());
  }

  // идентификатор пользователя из токена; подпись и срок проверяет декодер
  public long verify(String token) {
    try {
      return Long.parseLong(jwtDecoder.decode(token).getSubject());
    } catch (JwtException | NumberFormatException e) {
      throw new BadCredentialsException("invalid token", e);
    }
  }
}
