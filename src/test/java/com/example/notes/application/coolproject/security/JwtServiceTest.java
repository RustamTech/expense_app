package com.example.notes.application.coolproject.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link JwtService}.
 *
 * <p>The service is instantiated directly (no Spring context): the secret and expiration that are
 * normally injected via {@code @Value} are set through reflection, so the tests stay fast and do
 * not need the database.
 */
class JwtServiceTest {

  /** Base64-encoded 256-bit HMAC key, test-only. */
  private static final String SECRET =
      "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

  /** A different valid key, used to simulate a token signed by someone else. */
  private static final String FOREIGN_SECRET =
      "6A586E3272357538782F413F4428472B4B6250645367566B5970404E63526655";

  private static final long ONE_HOUR_MS = 3_600_000L;

  private JwtService jwtService;
  private UserDetails user;

  @BeforeEach
  void setUp() {
    jwtService = newJwtService(SECRET, ONE_HOUR_MS);
    user = User.withUsername("user@example.com").password("ignored").roles("USER").build();
  }

  @Test
  void generatedTokenCarriesUserEmailAsSubject() {
    String token = jwtService.generateToken(user);

    assertThat(jwtService.extractEmail(token)).isEqualTo("user@example.com");
  }

  @Test
  void generatedTokenKeepsExtraClaims() {
    String token = jwtService.generateToken(Map.of("role", "ADMIN"), user);

    String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

    assertThat(role).isEqualTo("ADMIN");
  }

  @Test
  void tokenIsValidForItsOwner() {
    String token = jwtService.generateToken(user);

    assertThat(jwtService.isTokenValid(token, user)).isTrue();
  }

  @Test
  void tokenIsInvalidForAnotherUser() {
    String token = jwtService.generateToken(user);
    UserDetails otherUser =
        User.withUsername("other@example.com").password("ignored").roles("USER").build();

    assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
  }

  @Test
  void expiredTokenIsRejected() {
    JwtService expiringService = newJwtService(SECRET, -1_000L);
    String expiredToken = expiringService.generateToken(user);

    assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, user))
        .isInstanceOf(ExpiredJwtException.class);
  }

  @Test
  void tokenSignedWithForeignKeyIsRejected() {
    String foreignToken = newJwtService(FOREIGN_SECRET, ONE_HOUR_MS).generateToken(user);

    assertThatThrownBy(() -> jwtService.extractClaim(foreignToken, Claims::getSubject))
        .isInstanceOf(SignatureException.class);
  }

  /**
   * Creates a {@link JwtService} with the given configuration, mimicking {@code @Value} injection.
   *
   * @param secret Base64-encoded HMAC key
   * @param expirationMs token lifetime in milliseconds (negative to produce an expired token)
   * @return a ready-to-use service instance
   */
  private static JwtService newJwtService(String secret, long expirationMs) {
    JwtService service = new JwtService();
    ReflectionTestUtils.setField(service, "secretKey", secret);
    ReflectionTestUtils.setField(service, "expiration", expirationMs);
    return service;
  }
}
