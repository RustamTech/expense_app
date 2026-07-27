package com.example.notes.application.coolproject.controller;

import com.example.notes.application.coolproject.config.SecurityConfig;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseCreateRequest;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseResponse;
import com.example.notes.application.coolproject.dto.expenseDto.ExpenseUpdateRequest;
import com.example.notes.application.coolproject.enums.CurrencyType;
import com.example.notes.application.coolproject.enums.StatusType;
import com.example.notes.application.coolproject.exception.custom.ExpenseNotFoundException;
import com.example.notes.application.coolproject.service.expense.ExpenseService;
import com.example.notes.application.coolproject.service.security.JwtService;
import com.example.notes.application.coolproject.service.security.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
@Import(SecurityConfig.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser
    void findAll_returns200WithList() throws Exception {
        List<ExpenseResponse> expenses = List.of(buildExpenseResponse(1L), buildExpenseResponse(2L));
        when(expenseService.findAll()).thenReturn(expenses);

        mockMvc.perform(get("/api/notes/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void findById_existing_returns200() throws Exception {
        ExpenseResponse response = buildExpenseResponse(1L);
        when(expenseService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/notes/expenses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Lunch"));
    }

    @Test
    @WithMockUser
    void findById_notFound_returns404() throws Exception {
        when(expenseService.findById(99L)).thenThrow(new ExpenseNotFoundException(99L));

        mockMvc.perform(get("/api/notes/expenses/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void create_validRequest_returns201() throws Exception {
        ExpenseCreateRequest request = new ExpenseCreateRequest(
                BigDecimal.TEN, "Lunch", null, CurrencyType.USD, LocalDate.now(), 1L, StatusType.PAID);
        ExpenseResponse response = buildExpenseResponse(1L);

        when(expenseService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/notes/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void create_negativeAmount_returns400() throws Exception {
        ExpenseCreateRequest request = new ExpenseCreateRequest(
                BigDecimal.valueOf(-5), "Lunch", null, CurrencyType.USD, LocalDate.now(), 1L, StatusType.PAID);

        mockMvc.perform(post("/api/notes/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void update_existingId_returns200() throws Exception {
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
                BigDecimal.valueOf(20), "Dinner", null, CurrencyType.EUR, LocalDate.now(), null, StatusType.PLANNED);
        ExpenseResponse response = buildExpenseResponse(1L);

        when(expenseService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/notes/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void delete_existing_returns204() throws Exception {
        mockMvc.perform(delete("/api/notes/expenses/1"))
                .andExpect(status().isNoContent());

        verify(expenseService).delete(1L);
    }

    @Test
    @WithMockUser
    void delete_notFound_returns404() throws Exception {
        doThrow(new ExpenseNotFoundException(99L)).when(expenseService).delete(99L);

        mockMvc.perform(delete("/api/notes/expenses/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void findAllByStatus_validStatus_returns200() throws Exception {
        List<ExpenseResponse> expenses = List.of(buildExpenseResponse(1L));
        when(expenseService.findAllByStatus(StatusType.PAID)).thenReturn(expenses);

        mockMvc.perform(get("/api/notes/expenses/status/PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void findAllByStatus_invalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/api/notes/expenses/status/INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void findAllByCategory_validCategory_returns200() throws Exception {
        List<ExpenseResponse> expenses = List.of(buildExpenseResponse(1L));
        when(expenseService.findAllByCategory(1L)).thenReturn(expenses);

        mockMvc.perform(get("/api/notes/expenses/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void findAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/notes/expenses"))
                .andExpect(status().isUnauthorized());
    }

    private ExpenseResponse buildExpenseResponse(Long id) {
        return new ExpenseResponse(id, BigDecimal.TEN, "Lunch", null, LocalDate.now(),
                null, StatusType.PAID, CurrencyType.USD, LocalDateTime.now(), LocalDateTime.now());
    }
}
