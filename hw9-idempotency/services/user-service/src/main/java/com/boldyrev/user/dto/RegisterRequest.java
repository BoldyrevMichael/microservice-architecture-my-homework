package com.boldyrev.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(max = 256) String username,
    @NotBlank @Size(min = 6, max = 72) String password,
    String firstName,
    String lastName,
    @Email String email,
    String phone) {}
