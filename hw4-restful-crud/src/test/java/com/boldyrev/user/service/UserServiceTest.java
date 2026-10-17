package com.boldyrev.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boldyrev.user.dto.CreateUserRequest;
import com.boldyrev.user.dto.UpdateUserRequest;
import com.boldyrev.user.dto.UserResponse;
import com.boldyrev.user.entity.User;
import com.boldyrev.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @InjectMocks private UserService userService;

  @Test
  void createSavesAllFields() {
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

    UserResponse created =
        userService.create(
            new CreateUserRequest("johndoe589", "John", "Doe", "bestjohn@doe.com", "+71002003040"));

    assertThat(created.username()).isEqualTo("johndoe589");
    assertThat(created.email()).isEqualTo("bestjohn@doe.com");
  }

  @Test
  void updateChangesProfileButNotUsername() {
    User user = new User();
    user.setUsername("johndoe589");
    user.setFirstName("John");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    UserResponse updated =
        userService.update(1L, new UpdateUserRequest("Julie", "Doe", null, "+71004242424"));

    assertThat(updated.firstName()).isEqualTo("Julie");
    assertThat(updated.username()).isEqualTo("johndoe589");
  }

  @Test
  void deleteRemovesExistingUser() {
    User user = new User();
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    userService.delete(1L);

    verify(userRepository).delete(user);
  }

  @Test
  void unknownIdThrowsNotFound() {
    when(userRepository.findById(42L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.get(42L))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage("user 42 not found");
  }
}
