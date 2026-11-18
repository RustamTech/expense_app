package com.example.notes.application.coolproject.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Issues and verifies HMAC-signed JWT access tokens. The subject of a token is the user email.
 *
 * <p>The signing key is decoded once at construction; a key shorter than 256 bits fails startup.
 */
@Service
public class JwtService {

  private final SecretKey signingKey;
  private final JwtProperties properties;

  /**
   * Creates the service.
   *
   * @param properties validated JWT settings
   * @throws io.jsonwebtoken.security.WeakKeyException if the secret is shorter than 256 bits
   */
  public JwtService(JwtProperties properties) {
    this.properties = properties;
    this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
  }

  /**
   * Issues a token for the given user.
   *
   * @param userDetails the authenticated user
   * @return compact signed JWT whose subject is {@link UserDetails#getUsername()}
   */
  public String generateToken(UserDetails userDetails) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(userDetails.getUsername())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(properties.expiration())))
        .signWith(signingKey)
        .compact();
  }

  /**
   * Verifies the token and returns its subject.
   *
   * @param token compact JWT
   * @return the email stored as the subject
   * @throws JwtException if the token is malformed, badly signed or expired
   */
  public String extractEmail(String token) {
    return Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  /**
   * Checks that a verified token belongs to the given, still enabled, user.
   *
   * @param token compact JWT
   * @param userDetails the user the token claims to belong to
   * @return {@code true} if the subject matches and the account is enabled
   * @throws JwtException if the token is malformed, badly signed or expired
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    return extractEmail(token).equals(userDetails.getUsername()) && userDetails.isEnabled();
  }
}
