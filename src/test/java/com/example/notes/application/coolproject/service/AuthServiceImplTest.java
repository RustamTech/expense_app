package com.example.notes.application.coolproject.service;

import static com.example.notes.application.coolproject.TestData.USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notes.application.coolproject.TestData;
import com.example.notes.application.coolproject.dto.auth.LoginRequest;
import com.example.notes.application.coolproject.dto.auth.RegisterRequest;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.exception.custom.ResourceAlreadyExistsException;
import com.example.notes.application.coolproject.repository.UserRepository;
import com.example.notes.application.coolproject.security.JwtService;
import com.example.notes.application.coolproject.service.auth.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
  void registerCreatesEnabledUserWithNormalizedEmailAndHashedPassword() {
    var request =
        new RegisterRequest(" John ", "Doe", "  User@Example.COM", "+994501234567", "password1");
    when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(false);
    when(passwordEncoder.encode("password1")).thenReturn("hashed");
    when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    when(jwtService.generateToken(any())).thenReturn("jwt");

    var response = authService.register(request);

    var saved = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(saved.capture());
    assertThat(saved.getValue().getEmail()).isEqualTo(USER_EMAIL);
    assertThat(saved.getValue().getFirstName()).isEqualTo("John");
    assertThat(saved.getValue().getPassword()).isEqualTo("hashed");
    assertThat(saved.getValue().getUserRole()).isEqualTo(UserRole.USER);
    assertThat(saved.getValue().isEnabled()).isTrue();
    assertThat(response.token()).isEqualTo("jwt");
    assertThat(response.role()).isEqualTo(UserRole.USER);
  }

  @Test
  void registerWithTakenEmailThrowsAndSavesNothing() {
    var request = new RegisterRequest("John", "Doe", USER_EMAIL, "+994501234567", "password1");
    when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(true);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(ResourceAlreadyExistsException.class);
    verify(userRepository, never()).save(any());
  }

  @Test
  void loginAuthenticatesNormalizedEmailAndReturnsTokenOfPrincipal() {
    UserEntity user = TestData.regularUser();
    var expectedCredentials = new UsernamePasswordAuthenticationToken(USER_EMAIL, "password1");
    when(authenticationManager.authenticate(expectedCredentials))
        .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    when(jwtService.generateToken(user)).thenReturn("jwt");

    var response = authService.login(new LoginRequest("USER@example.com", "password1"));

    assertThat(response.token()).isEqualTo("jwt");
    assertThat(response.email()).isEqualTo(USER_EMAIL);
    verify(userRepository, never()).findByEmail(any());
  }

  @Test
  void loginWithWrongPasswordPropagatesAuthenticationFailure() {
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThatThrownBy(() -> authService.login(new LoginRequest(USER_EMAIL, "wrong")))
        .isInstanceOf(BadCredentialsException.class);
    verify(jwtService, never()).generateToken(any());
  }
}
