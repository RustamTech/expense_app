package com.example.notes.application.coolproject.controller;

import com.example.notes.application.coolproject.config.SecurityConfig;
import com.example.notes.application.coolproject.security.JwtService;
import com.example.notes.application.coolproject.security.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base for {@code @WebMvcTest} slices: loads the real {@link SecurityConfig} (role rules, 401/403
 * rendering) and mocks the beans the JWT filter depends on.
 */
@Import(SecurityConfig.class)
abstract class AbstractControllerTest {

  protected static final String JSON = "application/json";

  @Autowired protected MockMvc mockMvc;

  @Autowired protected ObjectMapper objectMapper;

  @MockitoBean protected JwtService jwtService;

  @MockitoBean protected UserDetailsServiceImpl userDetailsService;

  protected String json(Object value) throws Exception {
    return objectMapper.writeValueAsString(value);
  }
}
