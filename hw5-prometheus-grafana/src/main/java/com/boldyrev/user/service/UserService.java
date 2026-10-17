package com.boldyrev.user.service;

import com.boldyrev.user.dto.CreateUserRequest;
import com.boldyrev.user.dto.UpdateUserRequest;
import com.boldyrev.user.dto.UserResponse;
import com.boldyrev.user.entity.User;
import com.boldyrev.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public UserResponse create(CreateUserRequest request) {
    User user = new User();
    user.setUsername(request.username());
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setEmail(request.email());
    user.setPhone(request.phone());
    return UserResponse.from(userRepository.save(user));
  }

  @Transactional(readOnly = true)
  public UserResponse get(long id) {
    return UserResponse.from(find(id));
  }

  @Transactional
  public UserResponse update(long id, UpdateUserRequest request) {
    User user = find(id);
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setEmail(request.email());
    user.setPhone(request.phone());
    return UserResponse.from(user);
  }

  @Transactional
  public void delete(long id) {
    userRepository.delete(find(id));
  }

  private User find(long id) {
    return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
  }
}
