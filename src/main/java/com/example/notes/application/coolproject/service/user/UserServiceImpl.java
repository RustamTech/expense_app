package com.example.notes.application.coolproject.service.user;

import com.example.notes.application.coolproject.dto.user.UserResponse;
import com.example.notes.application.coolproject.dto.user.UserUpdateRequest;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;
import com.example.notes.application.coolproject.mapper.UserMapper;
import com.example.notes.application.coolproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  @Transactional(readOnly = true)
  public UserResponse getCurrentUser(String email) {
    log.info("Fetching profile for user: {}", email);
    UserEntity user =
        userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    return userMapper.toResponse(user);
  }

  @Override
  @Transactional
  public UserResponse updateCurrentUser(String email, UserUpdateRequest request) {
    log.info("Updating profile for user: {}", email);
    UserEntity user =
        userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setPhoneNumber(request.phoneNumber());
    UserEntity updated = userRepository.save(user);
    log.info("Successfully updated profile for user: {}", email);
    return userMapper.toResponse(updated);
  }
}
