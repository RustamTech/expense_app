package com.example.notes.application.coolproject.security;

import static com.example.notes.application.coolproject.TestData.USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.notes.application.coolproject.TestData;
import com.example.notes.application.coolproject.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserDetailsServiceImpl userDetailsService;

  @Test
  void loadsUserByNormalizedEmail() {
    when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(TestData.regularUser()));

    var user = userDetailsService.loadUserByUsername("  User@Example.COM ");

    assertThat(user.getUsername()).isEqualTo(USER_EMAIL);
  }

  @Test
  void unknownEmailThrowsWithoutEchoingIt() {
    when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost@example.com"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageNotContaining("ghost");
  }
}
