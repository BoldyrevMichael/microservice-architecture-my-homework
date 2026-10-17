package com.boldyrev.user.controller;

import com.boldyrev.user.dto.CreateUserRequest;
import com.boldyrev.user.dto.UpdateUserRequest;
import com.boldyrev.user.dto.UserResponse;
import com.boldyrev.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
    return userService.create(request);
  }

  @GetMapping("/{id}")
  public UserResponse get(@PathVariable long id) {
    return userService.get(id);
  }

  @PutMapping("/{id}")
  public UserResponse update(@PathVariable long id, @Valid @RequestBody UpdateUserRequest request) {
    return userService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {
    userService.delete(id);
  }
}
