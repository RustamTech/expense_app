package com.example.notes.application.coolproject.service;

import com.example.notes.application.coolproject.dto.userDto.UserResponse;
import com.example.notes.application.coolproject.dto.userDto.UserUpdateRequest;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.exception.custom.UserNotFoundException;
import com.example.notes.application.coolproject.mapper.UserMapper;
import com.example.notes.application.coolproject.repository.UserRepository;
import com.example.notes.application.coolproject.service.user.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getCurrentUser_existingEmail_returnsUserResponse() {
        UserEntity user = buildUser();
        UserResponse response = buildUserResponse();

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.getCurrentUser("john@test.com");

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getCurrentUser_notFound_throwsUserNotFoundException() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser("missing@test.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateCurrentUser_existingUser_updatesAndReturnsResponse() {
        UserUpdateRequest request = new UserUpdateRequest("Jane", "Smith", "+9999999999");
        UserEntity user = buildUser();
        UserResponse response = buildUserResponse();

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.updateCurrentUser("john@test.com", request);

        assertThat(result).isEqualTo(response);
        verify(userRepository).save(user);
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getPhoneNumber()).isEqualTo("+9999999999");
    }

    @Test
    void updateCurrentUser_notFound_throwsUserNotFoundException() {
        UserUpdateRequest request = new UserUpdateRequest("Jane", "Smith", "+9999999999");
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateCurrentUser("missing@test.com", request))
                .isInstanceOf(UserNotFoundException.class);
    }

    private UserEntity buildUser() {
        UserEntity user = new UserEntity();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@test.com");
        user.setPhoneNumber("+1234567890");
        user.setPassword("hashed");
        user.setUserRole(UserRole.USER);
        user.setEnabled(true);
        return user;
    }

    private UserResponse buildUserResponse() {
        return new UserResponse(1L, "John", "Doe", "john@test.com", "+1234567890", UserRole.USER, LocalDateTime.now());
    }
}
