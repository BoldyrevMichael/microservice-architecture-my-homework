package com.boldyrev.user.dto;

import jakarta.validation.constraints.Email;

// username и пароль через профиль не меняются
public record UpdateProfileRequest(
    String firstName, String lastName, @Email String email, String phone) {}
