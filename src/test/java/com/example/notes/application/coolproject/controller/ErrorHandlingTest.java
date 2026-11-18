package com.example.notes.application.coolproject.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notes.application.coolproject.service.expense.ExpenseService;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Verifies that every kind of client or server error is answered with an RFC 9457 problem body
 * carrying the right status and {@code code}, using the expense endpoints as the vehicle.
 */
@WebMvcTest(ExpenseController.class)
class ErrorHandlingTest extends AbstractControllerTest {

  private static final String USER_EMAIL = "user@example.com";
  private static final String EXPENSES = "/api/notes/expenses";
  private static final String PROBLEM_JSON = "application/problem+json";

  private static final String VALID_BODY =
      """
      {"amount": 12.5, "description": "Lunch", "currencyType": "AZN",
       "date": "2026-01-14", "categoryId": 1, "statusType": "PAID"}
      """;

  @MockitoBean private ExpenseService expenseService;

  @Nested
  @WithMockUser(username = USER_EMAIL)
  class RequestBody {

    @Test
    void malformedJsonReturns400MalformedJson() throws Exception {
      mockMvc
          .perform(post(EXPENSES).contentType(JSON).content("{\"amount\": 12, "))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
          .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
          .andExpect(jsonPath("$.detail").value("Malformed JSON request body"))
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.timestamp").exists())
          .andExpect(jsonPath("$.instance").value(EXPENSES));
    }

    @Test
    void missingBodyReturns400MalformedJson() throws Exception {
      mockMvc
          .perform(post(EXPENSES).contentType(JSON))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
          .andExpect(jsonPath("$.detail").value("Request body is missing or unreadable"));
    }

    @Test
    void unknownEnumValueNamesFieldAndAllowedValues() throws Exception {
      String body = VALID_BODY.replace("\"AZN\"", "\"UZS\"");

      mockMvc
          .perform(post(EXPENSES).contentType(JSON).content(body))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
          .andExpect(
              jsonPath("$.detail")
                  .value(
                      "Invalid value 'UZS' for field 'currencyType'. Allowed values: [AZN, USD, EUR]"));
    }

    @Test
    void invalidDateNamesTheField() throws Exception {
      String body = VALID_BODY.replace("2026-01-14", "2026-13-45");

      mockMvc
          .perform(post(EXPENSES).contentType(JSON).content(body))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.detail").value(containsString("field 'date'")));
    }

    @Test
    void wrongJsonTypeNamesTheField() throws Exception {
      String body = VALID_BODY.replace("\"categoryId\": 1", "\"categoryId\": {\"id\": 1}");

      mockMvc
          .perform(post(EXPENSES).contentType(JSON).content(body))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.detail").value("Invalid type for field 'categoryId'"));
    }

    @Test
    void validationErrorsAreListedPerField() throws Exception {
      String body =
          """
          {"amount": -5, "description": " ", "currencyType": "AZN",
           "date": "2026-01-14", "statusType": "PAID"}
          """;

      mockMvc
          .perform(post(EXPENSES).contentType(JSON).content(body))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
          .andExpect(jsonPath("$.errors.length()").value(3))
          .andExpect(jsonPath("$.errors[?(@.field == 'amount')]").exists())
          .andExpect(jsonPath("$.errors[?(@.field == 'description')]").exists())
          .andExpect(jsonPath("$.errors[?(@.field == 'categoryId')]").exists());
    }

    @Test
    void unsupportedContentTypeReturns415() throws Exception {
      mockMvc
          .perform(post(EXPENSES).contentType(MediaType.TEXT_PLAIN).content("hello"))
          .andExpect(status().isUnsupportedMediaType())
          .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }
  }

  @Nested
  @WithMockUser(username = USER_EMAIL)
  class RequestLine {

    @Test
    void nonNumericIdReturns400InvalidParameter() throws Exception {
      mockMvc
          .perform(get(EXPENSES + "/abc"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
          .andExpect(jsonPath("$.detail").value("Invalid value 'abc' for parameter 'id'"));
    }

    @Test
    void unknownStatusInPathListsAllowedValues() throws Exception {
      mockMvc
          .perform(get(EXPENSES + "/status/DONE"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
          .andExpect(
              jsonPath("$.detail")
                  .value(containsString("Allowed values: [PENDING, PLANNED, PAID]")));
    }

    @Test
    void unknownUrlReturns404ResourceNotFound() throws Exception {
      mockMvc
          .perform(get("/api/notes/does-not-exist"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
          .andExpect(jsonPath("$.detail").value("No endpoint GET /api/notes/does-not-exist"));
    }

    @Test
    void unsupportedMethodReturns405WithAllowHeader() throws Exception {
      mockMvc
          .perform(patch(EXPENSES))
          .andExpect(status().isMethodNotAllowed())
          .andExpect(header().exists("Allow"))
          .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void unacceptableResponseTypeReturns406() throws Exception {
      mockMvc
          .perform(get(EXPENSES).accept(MediaType.IMAGE_PNG))
          .andExpect(status().isNotAcceptable());
    }
  }

  @Nested
  @WithMockUser(username = USER_EMAIL)
  class ServerSide {

    @Test
    void unexpectedExceptionReturns500WithoutInternals() throws Exception {
      when(expenseService.findAll(any()))
          .thenThrow(new IllegalStateException("db password is hunter2"));

      mockMvc
          .perform(get(EXPENSES))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
          .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
          .andExpect(content().string(not(containsString("hunter2"))));
    }

    @Test
    void dataIntegrityViolationReturns409WithoutSqlDetails() throws Exception {
      when(expenseService.findAll(any()))
          .thenThrow(new DataIntegrityViolationException("duplicate key value violates uq_x"));

      mockMvc
          .perform(get(EXPENSES))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value("DATA_CONFLICT"))
          .andExpect(content().string(not(containsString("uq_x"))));
    }
  }

  @Nested
  class Authentication {

    @Test
    void missingTokenReturns401AuthenticationRequired() throws Exception {
      mockMvc
          .perform(get(EXPENSES))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
          .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void invalidTokenReturns401InvalidToken() throws Exception {
      when(jwtService.extractEmail(anyString())).thenThrow(new MalformedJwtException("bad"));

      mockMvc
          .perform(get(EXPENSES).header("Authorization", "Bearer not-a-jwt"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void unknownUrlWithoutTokenReturns401NotLeakingRoutes() throws Exception {
      mockMvc.perform(get("/api/notes/does-not-exist")).andExpect(status().isUnauthorized());
    }
  }
}
