package com.boldyrev.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boldyrev.user.config.WebConfig;
import com.boldyrev.user.dto.CreateUserRequest;
import com.boldyrev.user.dto.UpdateUserRequest;
import com.boldyrev.user.dto.UserResponse;
import com.boldyrev.user.service.UserNotFoundException;
import com.boldyrev.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(UserController.class)
@Import(WebConfig.class)
class UserControllerTest {

  private static final UserResponse JOHN =
      new UserResponse(1L, "johndoe589", "John", "Doe", "bestjohn@doe.com", "+71002003040");

  @Autowired private MockMvcTester mvc;
  @MockitoBean private UserService userService;

  @Test
  void createReturnsCreatedUser() {
    when(userService.create(any(CreateUserRequest.class))).thenReturn(JOHN);

    assertThat(
            mvc.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"username":"johndoe589","firstName":"John","lastName":"Doe",
                     "email":"bestjohn@doe.com","phone":"+71002003040"}
                    """))
        .hasStatus(HttpStatus.CREATED)
        .bodyJson()
        .extractingPath("$.id")
        .isEqualTo(1);
  }

  @Test
  void createWithoutUsernameIsBadRequest() {
    assertThat(
            mvc.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\"}"))
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .extractingPath("$.code")
        .isEqualTo(400);
  }

  @Test
  void createWithDuplicateUsernameIsConflict() {
    when(userService.create(any(CreateUserRequest.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate"));

    assertThat(
            mvc.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"johndoe589\"}"))
        .hasStatus(HttpStatus.CONFLICT);
  }

  @Test
  void getReturnsUser() {
    when(userService.get(1L)).thenReturn(JOHN);

    assertThat(mvc.get().uri("/user/1"))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .extractingPath("$.username")
        .isEqualTo("johndoe589");
  }

  @Test
  void getWithTrailingSlashReturnsUser() {
    when(userService.get(1L)).thenReturn(JOHN);

    assertThat(mvc.get().uri("/user/1/")).hasStatus(HttpStatus.OK);
  }

  @Test
  void getUnknownUserIsNotFound() {
    when(userService.get(42L)).thenThrow(new UserNotFoundException(42));

    assertThat(mvc.get().uri("/user/42"))
        .hasStatus(HttpStatus.NOT_FOUND)
        .bodyJson()
        .isEqualTo("{\"code\":404,\"message\":\"user 42 not found\"}");
  }

  @Test
  void updateReturnsUpdatedUser() {
    when(userService.update(eq(1L), any(UpdateUserRequest.class)))
        .thenReturn(
            new UserResponse(1L, "johndoe589", "Julie", "Doe", "bestjohn@doe.com", "+71004242424"));

    assertThat(
            mvc.put()
                .uri("/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Julie\",\"lastName\":\"Doe\"}"))
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .extractingPath("$.firstName")
        .isEqualTo("Julie");
  }

  @Test
  void deleteReturnsNoContent() {
    assertThat(mvc.delete().uri("/user/1")).hasStatus(HttpStatus.NO_CONTENT);
    verify(userService).delete(1L);
  }
}
