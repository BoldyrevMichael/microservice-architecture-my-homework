package com.boldyrev.user.dto;

import com.boldyrev.user.entity.User;

public record UserResponse(
    Long id, String username, String firstName, String lastName, String email, String phone) {

  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getPhone());
  }
}
