package com.boldyrev.user.service;

import com.boldyrev.user.dto.UpdateProfileRequest;
import com.boldyrev.user.dto.UserResponse;
import com.boldyrev.user.entity.User;
import com.boldyrev.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

  private final UserRepository userRepository;

  @Autowired
  public ProfileService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public UserResponse get(long userId) {
    return UserResponse.from(find(userId));
  }

  @Transactional
  public UserResponse update(long userId, UpdateProfileRequest request) {
    User user = find(userId);
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setEmail(request.email());
    user.setPhone(request.phone());
    return UserResponse.from(user);
  }

  private User find(long userId) {
    return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
  }
}
