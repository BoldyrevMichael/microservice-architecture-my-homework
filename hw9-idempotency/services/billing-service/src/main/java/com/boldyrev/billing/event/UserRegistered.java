package com.boldyrev.billing.event;

public record UserRegistered(long userId, String username, String email) {}
