package com.boldyrev.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.boldyrev.user.config.WebConfig;
import com.boldyrev.user.dto.UserResponse;
import com.boldyrev.user.security.GatewayUserFilter;
import com.boldyrev.user.security.SecurityConfig;
import com.boldyrev.user.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(ProfileController.class)
@Import({SecurityConfig.class, WebConfig.class})
class ProfileControllerTest {

  @Autowired private MockMvcTester mvc;
  @MockitoBean private ProfileService profileService;

  @Test
  void profileWithoutGatewayHeaderIsUnauthorized() {
    assertThat(mvc.get().uri("/profile")).hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void profileReturnsOwnUserById() {
    when(profileService.get(7L))
        .thenReturn(new UserResponse(7L, "johndoe", "John", "Doe", "j@d.com", "+7100"));

    assertThat(mvc.get().uri("/profile").header(GatewayUserFilter.USER_ID_HEADER, "7"))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .extractingPath("$.username")
        .isEqualTo("johndoe");
  }
}
