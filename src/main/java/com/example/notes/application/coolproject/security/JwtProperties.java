package com.example.notes.application.coolproject.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT settings bound from {@code jwt.*}. Validated at startup, so the application refuses to start
 * without a secret instead of silently signing tokens with a well-known default.
 *
 * @param secret Base64-encoded HMAC-SHA key, at least 256 bits; supplied via {@code JWT_SECRET}
 * @param expiration token lifetime; a plain number is read as milliseconds
 */
@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(@NotBlank String secret, @NotNull Duration expiration) {}
