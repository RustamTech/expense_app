package com.example.notes.application.coolproject.service;

import static com.example.notes.application.coolproject.TestData.USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.notes.application.coolproject.TestData;
import com.example.notes.application.coolproject.dto.user.UserResponse;
import com.example.notes.application.coolproject.dto.user.UserUpdateRequest;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;
import com.example.notes.application.coolproject.mapper.UserMapper;
import com.example.notes.application.coolproject.repository.UserRepository;
import com.example.notes.application.coolproject.service.user.UserServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private UserMapper userMapper;

  @InjectMocks private UserServiceImpl userService;

  @Test
  void returnsProfileOfCaller() {
    UserEntity user = TestData.regularUser();
    UserResponse response = TestData.userResponse(1L, USER_EMAIL, UserRole.USER);
    when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
    when(userMapper.toResponse(user)).thenReturn(response);

    assertThat(userService.getCurrentUser(USER_EMAIL)).isEqualTo(response);
  }

  @Test
  void missingCallerThrowsUserNotFound() {
    when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getCurrentUser(USER_EMAIL))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void updatesOnlyEditableFieldsAndTrimsNames() {
    UserEntity user = TestData.regularUser();
    when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

    userService.updateCurrentUser(
        USER_EMAIL, new UserUpdateRequest(" Jane ", "Roe", "+15550000000"));

    assertThat(user.getFirstName()).isEqualTo("Jane");
    assertThat(user.getLastName()).isEqualTo("Roe");
    assertThat(user.getPhoneNumber()).isEqualTo("+15550000000");
    assertThat(user.getEmail()).isEqualTo(USER_EMAIL);
    assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
  }

  @Test
  void updateOfMissingCallerThrowsUserNotFound() {
    when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());
    var request = new UserUpdateRequest("Jane", "Roe", "+15550000000");

    assertThatThrownBy(() -> userService.updateCurrentUser(USER_EMAIL, request))
        .isInstanceOf(UserNotFoundException.class);
  }
}
