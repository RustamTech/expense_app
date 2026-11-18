package com.example.notes.application.coolproject.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.WeakKeyException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

  /** Base64 of a 256-bit test-only key. */
  private static final String SECRET =
      "dGVzdC1vbmx5LXNlY3JldC1rZXktZm9yLWp3dC1zZXJ2aWNlLXVuaXQtdGVzdHM=";

  /** A different valid key, used to simulate a token signed by someone else. */
  private static final String FOREIGN_SECRET =
      "Zm9yZWlnbi1zZWNyZXQta2V5LXRoYXQtaXMtYWxzby1sb25nLWVub3VnaC0yNTY=";

  private static final Duration ONE_HOUR = Duration.ofHours(1);

  private JwtService jwtService;
  private UserDetails user;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(new JwtProperties(SECRET, ONE_HOUR));
    user = account("user@example.com", true);
  }

  @Test
  void tokenCarriesEmailAsSubject() {
    String token = jwtService.generateToken(user);

    assertThat(jwtService.extractEmail(token)).isEqualTo("user@example.com");
  }

  @Test
  void tokenIsValidForItsEnabledOwner() {
    assertThat(jwtService.isTokenValid(jwtService.generateToken(user), user)).isTrue();
  }

  @Test
  void tokenIsInvalidForAnotherUser() {
    String token = jwtService.generateToken(user);

    assertThat(jwtService.isTokenValid(token, account("other@example.com", true))).isFalse();
  }

  @Test
  void tokenOfBlockedUserIsInvalid() {
    String token = jwtService.generateToken(user);

    assertThat(jwtService.isTokenValid(token, account("user@example.com", false))).isFalse();
  }

  @Test
  void expiredTokenIsRejected() {
    var expiring = new JwtService(new JwtProperties(SECRET, Duration.ofSeconds(-1)));
    String expired = expiring.generateToken(user);

    assertThatThrownBy(() -> jwtService.extractEmail(expired))
        .isInstanceOf(ExpiredJwtException.class);
  }

  @Test
  void tokenSignedWithForeignKeyIsRejected() {
    String foreign =
        new JwtService(new JwtProperties(FOREIGN_SECRET, ONE_HOUR)).generateToken(user);

    assertThatThrownBy(() -> jwtService.extractEmail(foreign))
        .isInstanceOf(SignatureException.class);
  }

  @Test
  void garbageTokenIsRejected() {
    assertThatThrownBy(() -> jwtService.extractEmail("not-a-jwt"))
        .isInstanceOf(MalformedJwtException.class);
  }

  @Test
  void keyShorterThan256BitsFailsStartup() {
    var weak = new JwtProperties("c2hvcnQ=", ONE_HOUR);

    assertThatThrownBy(() -> new JwtService(weak)).isInstanceOf(WeakKeyException.class);
  }

  private static UserDetails account(String email, boolean enabled) {
    return User.withUsername(email).password("x").roles("USER").disabled(!enabled).build();
  }
}
