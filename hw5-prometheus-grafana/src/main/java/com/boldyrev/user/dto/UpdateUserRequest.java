package com.boldyrev.user.dto;

import jakarta.validation.constraints.Email;

// username после создания не меняется
public record UpdateUserRequest(
    String firstName, String lastName, @Email String email, String phone) {}
