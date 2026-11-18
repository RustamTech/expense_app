package com.example.notes.application.coolproject.entity;

import com.example.notes.application.coolproject.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * An account. Doubles as the Spring Security principal: the username is the (normalized) email and
 * the single authority is {@code ROLE_<userRole>}. A disabled account can neither log in nor use
 * previously issued tokens.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity extends BaseEntity implements UserDetails {

  private static final String ROLE_PREFIX = "ROLE_";

  @Column(nullable = false, length = 50)
  private String firstName;

  @Column(nullable = false, length = 50)
  private String lastName;

  @Column(unique = true, nullable = false, length = 254)
  private String email;

  @Column(nullable = false, length = 20)
  private String phoneNumber;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole userRole;

  @Column(nullable = false)
  private boolean enabled;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + userRole.name()));
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
