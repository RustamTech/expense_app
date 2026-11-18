package com.example.notes.application.coolproject.service.auth;

import com.example.notes.application.coolproject.dto.auth.AuthResponse;
import com.example.notes.application.coolproject.dto.auth.LoginRequest;
import com.example.notes.application.coolproject.dto.auth.RegisterRequest;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.exception.custom.ResourceAlreadyExistsException;
import com.example.notes.application.coolproject.repository.UserRepository;
import com.example.notes.application.coolproject.security.EmailNormalizer;
import com.example.notes.application.coolproject.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@inheritDoc} Passwords are hashed with the configured {@link PasswordEncoder}. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String email = EmailNormalizer.normalize(request.email());
    if (userRepository.existsByEmail(email)) {
      throw new ResourceAlreadyExistsException("Email is already registered");
    }
    UserEntity saved = userRepository.save(newUser(request, email));
    log.info("Registered user with id {}", saved.getId());
    return toAuthResponse(saved);
  }

  /** {@inheritDoc} The authenticated principal is reused, so no second user lookup is made. */
  @Override
  public AuthResponse login(LoginRequest request) {
    var credentials =
        new UsernamePasswordAuthenticationToken(
            EmailNormalizer.normalize(request.email()), request.password());
    var authentication = authenticationManager.authenticate(credentials);
    if (!(authentication.getPrincipal() instanceof UserEntity user)) {
      throw new AuthenticationServiceException("Unexpected principal type");
    }
    log.info("User with id {} logged in", user.getId());
    return toAuthResponse(user);
  }

  private UserEntity newUser(RegisterRequest request, String normalizedEmail) {
    UserEntity user = new UserEntity();
    user.setFirstName(request.firstName().trim());
    user.setLastName(request.lastName().trim());
    user.setEmail(normalizedEmail);
    user.setPhoneNumber(request.phoneNumber());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setUserRole(UserRole.USER);
    user.setEnabled(true);
    return user;
  }

  private AuthResponse toAuthResponse(UserEntity user) {
    return new AuthResponse(
        jwtService.generateToken(user),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getUserRole());
  }
}
