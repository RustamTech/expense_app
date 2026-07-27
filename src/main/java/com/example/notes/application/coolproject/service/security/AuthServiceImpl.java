package com.example.notes.application.coolproject.service.security;

import com.example.notes.application.coolproject.dto.auth.AuthResponse;
import com.example.notes.application.coolproject.dto.auth.LoginRequest;
import com.example.notes.application.coolproject.dto.auth.RegisterRequest;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.exception.custom.ResourceAlreadyExistsException;
import com.example.notes.application.coolproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

    @Slf4j
    @Service
    @RequiredArgsConstructor
    public class AuthServiceImpl implements AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        @Override
        @Transactional
        public AuthResponse register(RegisterRequest request) {
            log.info("Registering new user with email: {}", request.email());

            if (userRepository.existsByEmail(request.email())) {
                log.error("Email already exists: {}", request.email());
                throw new ResourceAlreadyExistsException(request.email());
            }

            UserEntity user = new UserEntity();
            user.setFirstName(request.firstName());
            user.setLastName(request.lastName());
            user.setEmail(request.email());
            user.setPhoneNumber(request.phoneNumber());
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setUserRole(UserRole.USER);
            user.setEnabled(true);

            UserEntity saved = userRepository.save(user);
            log.info("Successfully registered user with id: {}", saved.getId());

            String token = jwtService.generateToken(saved);
            return new AuthResponse(
                    token,
                    saved.getEmail(),
                    saved.getFirstName(),
                    saved.getLastName()
            );
        }

        @Override
        public AuthResponse login(LoginRequest request) {
            log.info("Logging in user with email: {}", request.email());

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            UserEntity user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtService.generateToken(user);
            log.info("Successfully logged in user: {}", request.email());

            return new AuthResponse(
                    token,
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName()
            );
        }
    }