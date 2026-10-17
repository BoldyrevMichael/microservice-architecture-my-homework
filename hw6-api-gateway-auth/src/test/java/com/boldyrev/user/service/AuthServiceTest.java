package com.boldyrev.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boldyrev.user.dto.LoginRequest;
import com.boldyrev.user.dto.RegisterRequest;
import com.boldyrev.user.dto.TokenResponse;
import com.boldyrev.user.dto.UserResponse;
import com.boldyrev.user.entity.User;
import com.boldyrev.user.repository.UserRepository;
import com.boldyrev.user.security.JwtProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  @Mock private UserRepository userRepository;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    var key =
        new SecretKeySpec(
            "test-secret-test-secret-test-secret-1234".getBytes(StandardCharsets.UTF_8),
            "HmacSHA256");
    authService =
        new AuthService(
            userRepository,
            passwordEncoder,
            new NimbusJwtEncoder(new ImmutableSecret<>(key)),
            NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build(),
            new JwtProperties("unused", Duration.ofMinutes(5)));
  }

  @Test
  void registerStoresPasswordHashNotPassword() {
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
    ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);

    UserResponse created =
        authService.register(
            new RegisterRequest("johndoe", "secret123", "John", "Doe", "j@d.com", "+7100"));

    assertThat(created.username()).isEqualTo("johndoe");
    verify(userRepository).save(saved.capture());
    assertThat(saved.getValue().getPasswordHash()).isNotEqualTo("secret123");
    assertThat(passwordEncoder.matches("secret123", saved.getValue().getPasswordHash())).isTrue();
  }

  @Test
  void loginIssuesTokenThatVerifiesToUserId() {
    when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user(7L, "secret123")));

    TokenResponse token = authService.login(new LoginRequest("johndoe", "secret123"));

    assertThat(token.expiresIn()).isEqualTo(300);
    assertThat(authService.verify(token.token())).isEqualTo(7L);
  }

  @Test
  void loginWithWrongPasswordIsRejected() {
    when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user(7L, "secret123")));

    LoginRequest wrongPassword = new LoginRequest("johndoe", "wrong");

    assertThatThrownBy(() -> authService.login(wrongPassword))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void verifyRejectsForgedToken() {
    assertThatThrownBy(() -> authService.verify("not.a.token"))
        .isInstanceOf(BadCredentialsException.class);
  }

  private User user(long id, String password) {
    User user = new User();
    user.setId(id);
    user.setUsername("johndoe");
    user.setPasswordHash(passwordEncoder.encode(password));
    return user;
  }
}
