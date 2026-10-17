package com.boldyrev.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank @Size(max = 256) String username,
    String firstName,
    String lastName,
    @Email String email,
    String phone) {}
