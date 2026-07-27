package com.example.notes.application.coolproject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notes.application.coolproject.dto.auth.AuthResponse;
import com.example.notes.application.coolproject.dto.auth.LoginRequest;
import com.example.notes.application.coolproject.dto.auth.RegisterRequest;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.exception.custom.ResourceAlreadyExistsException;
import com.example.notes.application.coolproject.repository.UserRepository;
import com.example.notes.application.coolproject.service.security.AuthServiceImpl;
import com.example.notes.application.coolproject.service.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtService jwtService;

  @Mock private AuthenticationManager authenticationManager;

  @InjectMocks private AuthServiceImpl authService;

  @Test
  void register_newEmail_savesUserAndReturnsToken() {
    RegisterRequest request =
        new RegisterRequest("John", "Doe", "john@test.com", "+1234567890", "password123");
    UserEntity saved = buildUser("john@test.com");

    when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("hashed");
    when(userRepository.save(any(UserEntity.class))).thenReturn(saved);
    when(jwtService.generateToken(saved)).thenReturn("jwt-token");

    AuthResponse response = authService.register(request);

    assertThat(response.token()).isEqualTo("jwt-token");
    assertThat(response.email()).isEqualTo("john@test.com");

    ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(captor.capture());
    UserEntity captured = captor.getValue();
    assertThat(captured.getUserRole()).isEqualTo(UserRole.USER);
    assertThat(captured.isEnabled()).isTrue();
  }

  @Test
  void register_duplicateEmail_throwsResourceAlreadyExistsException() {
    RegisterRequest request =
        new RegisterRequest("John", "Doe", "john@test.com", "+1234567890", "password123");
    when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(ResourceAlreadyExistsException.class);
  }

  @Test
  void login_validCredentials_returnsToken() {
    LoginRequest request = new LoginRequest("john@test.com", "password123");
    UserEntity user = buildUser("john@test.com");

    when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
    when(jwtService.generateToken(user)).thenReturn("jwt-token");

    AuthResponse response = authService.login(request);

    assertThat(response.token()).isEqualTo("jwt-token");
    verify(authenticationManager)
        .authenticate(new UsernamePasswordAuthenticationToken("john@test.com", "password123"));
  }

  private UserEntity buildUser(String email) {
    UserEntity user = new UserEntity();
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail(email);
    user.setPhoneNumber("+1234567890");
    user.setPassword("hashed");
    user.setUserRole(UserRole.USER);
    user.setEnabled(true);
    return user;
  }
}
