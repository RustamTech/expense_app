package com.example.notes.application.coolproject.service.user;

import com.example.notes.application.coolproject.config.AdminProperties;
import com.example.notes.application.coolproject.entity.UserEntity;
import com.example.notes.application.coolproject.enums.UserRole;
import com.example.notes.application.coolproject.repository.UserRepository;
import com.example.notes.application.coolproject.security.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the first administrator on startup, because registration only ever creates {@code USER}
 * accounts. Idempotent: an existing account with the configured email is left untouched.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements ApplicationRunner {

  private static final String ADMIN_FIRST_NAME = "System";
  private static final String ADMIN_LAST_NAME = "Administrator";
  private static final String ADMIN_PHONE = "+000000000";

  private final AdminProperties adminProperties;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Creates the administrator if credentials are configured and the account does not exist yet.
   *
   * @param args application arguments, unused
   */
  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (!adminProperties.isConfigured()) {
      log.info("No bootstrap administrator configured (ADMIN_EMAIL / ADMIN_PASSWORD)");
      return;
    }
    String email = EmailNormalizer.normalize(adminProperties.email());
    if (userRepository.existsByEmail(email)) {
      log.info("Bootstrap administrator already exists");
      return;
    }
    UserEntity saved = userRepository.save(newAdmin(email));
    log.info("Created bootstrap administrator with id {}", saved.getId());
  }

  private UserEntity newAdmin(String email) {
    UserEntity admin = new UserEntity();
    admin.setFirstName(ADMIN_FIRST_NAME);
    admin.setLastName(ADMIN_LAST_NAME);
    admin.setPhoneNumber(ADMIN_PHONE);
    admin.setEmail(email);
    admin.setPassword(passwordEncoder.encode(adminProperties.password()));
    admin.setUserRole(UserRole.ADMIN);
    admin.setEnabled(true);
    return admin;
  }
}
