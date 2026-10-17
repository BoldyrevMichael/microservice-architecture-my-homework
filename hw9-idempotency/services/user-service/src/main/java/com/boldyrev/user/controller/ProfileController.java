package com.boldyrev.user.controller;

import com.boldyrev.user.dto.UpdateProfileRequest;
import com.boldyrev.user.dto.UserResponse;
import com.boldyrev.user.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// профиль только свой: id берётся из аутентификации, которую выставил GatewayUserFilter
@RestController
@RequestMapping("/profile")
public class ProfileController {

  private final ProfileService profileService;

  @Autowired
  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  @GetMapping
  public UserResponse get(@AuthenticationPrincipal String userId) {
    return profileService.get(Long.parseLong(userId));
  }

  @PutMapping
  public UserResponse update(
      @AuthenticationPrincipal String userId, @Valid @RequestBody UpdateProfileRequest request) {
    return profileService.update(Long.parseLong(userId), request);
  }
}
