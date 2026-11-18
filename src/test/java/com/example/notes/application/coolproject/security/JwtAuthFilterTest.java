package com.example.notes.application.coolproject.security;

import static com.example.notes.application.coolproject.TestData.USER_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.notes.application.coolproject.TestData;
import com.example.notes.application.coolproject.entity.UserEntity;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

  private static final String TOKEN = "token";

  @Mock private JwtService jwtService;
  @Mock private UserDetailsServiceImpl userDetailsService;
  @Mock private FilterChain chain;

  @InjectMocks private JwtAuthFilter filter;

  private final MockHttpServletRequest request = new MockHttpServletRequest();
  private final MockHttpServletResponse response = new MockHttpServletResponse();

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void requestWithoutHeaderStaysAnonymous() throws Exception {
    filter.doFilter(request, response, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    assertThat(request.getAttribute(JwtAuthFilter.INVALID_TOKEN_ATTRIBUTE)).isNull();
    verify(chain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void nonBearerHeaderIsIgnored() throws Exception {
    request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

    filter.doFilter(request, response, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verifyNoInteractions(jwtService);
  }

  @Test
  void validTokenAuthenticatesUserWithRole() throws Exception {
    UserEntity user = TestData.regularUser();
    request.addHeader("Authorization", "Bearer " + TOKEN);
    when(jwtService.extractEmail(TOKEN)).thenReturn(USER_EMAIL);
    when(userDetailsService.loadUserByUsername(USER_EMAIL)).thenReturn(user);
    when(jwtService.isTokenValid(TOKEN, user)).thenReturn(true);

    filter.doFilter(request, response, chain);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication.getName()).isEqualTo(USER_EMAIL);
    assertThat(authentication.getAuthorities())
        .extracting("authority")
        .containsExactly("ROLE_USER");
    verify(chain).doFilter(request, response);
  }

  @Test
  void expiredTokenIsMarkedInvalidAndRequestContinues() throws Exception {
    request.addHeader("Authorization", "Bearer " + TOKEN);
    when(jwtService.extractEmail(TOKEN)).thenThrow(new ExpiredJwtException(null, null, "expired"));

    filter.doFilter(request, response, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    assertThat(request.getAttribute(JwtAuthFilter.INVALID_TOKEN_ATTRIBUTE)).isEqualTo(true);
    verify(chain).doFilter(request, response);
  }

  @Test
  void tokenOfDeletedUserIsMarkedInvalid() throws Exception {
    request.addHeader("Authorization", "Bearer " + TOKEN);
    when(jwtService.extractEmail(TOKEN)).thenReturn(USER_EMAIL);
    when(userDetailsService.loadUserByUsername(USER_EMAIL))
        .thenThrow(new UsernameNotFoundException("gone"));

    filter.doFilter(request, response, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    assertThat(request.getAttribute(JwtAuthFilter.INVALID_TOKEN_ATTRIBUTE)).isEqualTo(true);
  }

  @Test
  void tokenOfBlockedUserIsMarkedInvalid() throws Exception {
    UserEntity blocked = TestData.regularUser();
    blocked.setEnabled(false);
    request.addHeader("Authorization", "Bearer " + TOKEN);
    when(jwtService.extractEmail(TOKEN)).thenReturn(USER_EMAIL);
    when(userDetailsService.loadUserByUsername(USER_EMAIL)).thenReturn(blocked);
    when(jwtService.isTokenValid(TOKEN, blocked)).thenReturn(false);

    filter.doFilter(request, response, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    assertThat(request.getAttribute(JwtAuthFilter.INVALID_TOKEN_ATTRIBUTE)).isEqualTo(true);
  }
}
