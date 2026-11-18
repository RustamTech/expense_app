package com.example.notes.application.coolproject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.notes.application.coolproject.config.AdminProperties;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.repository.UserRepository;
import com.example.notes.application.coolproject.service.user.AdminAccountInitializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminAccountInitializerTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @Test
  void createsEnabledAdminWithHashedPasswordWhenMissing() {
    var initializer = initializer(new AdminProperties(" Admin@Local.dev ", "secret123"));
    when(userRepository.existsByEmail("admin@local.dev")).thenReturn(false);
    when(passwordEncoder.encode("secret123")).thenReturn("hashed");
    when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

    initializer.run(null);

    var saved = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(saved.capture());
    assertThat(saved.getValue().getEmail()).isEqualTo("admin@local.dev");
    assertThat(saved.getValue().getUserRole()).isEqualTo(UserRole.ADMIN);
    assertThat(saved.getValue().getPassword()).isEqualTo("hashed");
    assertThat(saved.getValue().isEnabled()).isTrue();
  }

  @Test
  void leavesExistingAccountUntouched() {
    var initializer = initializer(new AdminProperties("admin@local.dev", "secret123"));
    when(userRepository.existsByEmail("admin@local.dev")).thenReturn(true);

    initializer.run(null);

    verify(userRepository, never()).save(any());
  }

  @Test
  void doesNothingWithoutCredentials() {
    initializer(new AdminProperties("", null)).run(null);

    verifyNoInteractions(userRepository, passwordEncoder);
  }

  private AdminAccountInitializer initializer(AdminProperties properties) {
    return new AdminAccountInitializer(properties, userRepository, passwordEncoder);
  }
}
