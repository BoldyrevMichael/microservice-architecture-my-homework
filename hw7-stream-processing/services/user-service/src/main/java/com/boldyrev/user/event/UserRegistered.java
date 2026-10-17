package com.boldyrev.user.event;

// событие в тему user.registered: по нему billing заводит счёт, notification запоминает email
public record UserRegistered(long userId, String username, String email) {}
