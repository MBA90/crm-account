package com.crm.account.web.rest;

import com.crm.account.domain.enums.AccountSource;
import com.crm.account.domain.enums.AccountStatus;
import com.crm.account.domain.enums.AccountType;
import com.crm.account.domain.enums.EmployeeBand;
import com.crm.account.dto.AccountDTO;
import com.crm.account.exception.DuplicateResourceException;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    private final UUID accountId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();

    @Test
    @DisplayName("GET /api/accounts lists every account when no filter is given")
    void listAll() throws Exception {
        when(accountService.getAll()).thenReturn(List.of(accountDTO(AccountStatus.ACTIVE)));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].accountId").value(accountId.toString()))
                .andExpect(jsonPath("$[0].accountType").value("prospect"))
                .andExpect(jsonPath("$[0].status").value("Active"));

        verify(accountService, never()).getByOwner(any(), any());
    }

    @Test
    @DisplayName("GET /api/accounts?ownerId= defaults the status filter to Active")
    void listByOwnerDefaultsToActive() throws Exception {
        when(accountService.getByOwner(ownerId, AccountStatus.ACTIVE)).thenReturn(List.of());

        mockMvc.perform(get("/api/accounts").param("ownerId", ownerId.toString()))
                .andExpect(status().isOk());

        verify(accountService).getByOwner(ownerId, AccountStatus.ACTIVE);
        verify(accountService, never()).getAll();
    }

    @Test
    @DisplayName("GET /api/accounts?ownerId=&status= honours an explicit status")
    void listByOwnerWithStatus() throws Exception {
        when(accountService.getByOwner(ownerId, AccountStatus.INACTIVE)).thenReturn(List.of());

        mockMvc.perform(get("/api/accounts")
                        .param("ownerId", ownerId.toString())
                        .param("status", "INACTIVE"))
                .andExpect(status().isOk());

        verify(accountService).getByOwner(ownerId, AccountStatus.INACTIVE);
    }

    @Test
    @DisplayName("GET /api/accounts/{id} returns the account")
    void getById() throws Exception {
        when(accountService.getById(accountId)).thenReturn(accountDTO(AccountStatus.ACTIVE));

        mockMvc.perform(get("/api/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legalName").value("Acme LLC"))
                .andExpect(jsonPath("$.employeeBand").value("11-50"))
                .andExpect(jsonPath("$.source").value("api"));
    }

    @Test
    @DisplayName("GET /api/accounts/{id} maps a missing account to 404")
    void getByIdNotFound() throws Exception {
        when(accountService.getById(accountId))
                .thenThrow(new ResourceNotFoundException("Account not found: " + accountId));

        mockMvc.perform(get("/api/accounts/{id}", accountId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Account not found: " + accountId));
    }

    @Test
    @DisplayName("GET /api/accounts/{id} rejects a malformed UUID with 400")
    void getByIdMalformedUuid() throws Exception {
        mockMvc.perform(get("/api/accounts/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/accounts returns 201 with a Location header")
    void create() throws Exception {
        when(accountService.create(any())).thenReturn(accountDTO(AccountStatus.ACTIVE));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDTO(null))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/api/accounts/" + accountId)))
                .andExpect(jsonPath("$.accountId").value(accountId.toString()));
    }

    @Test
    @DisplayName("POST /api/accounts maps a duplicate registration to 409")
    void createDuplicate() throws Exception {
        when(accountService.create(any()))
                .thenThrow(new DuplicateResourceException("already exists"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDTO(null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("already exists"));
    }

    @Test
    @DisplayName("POST /api/accounts reports the offending fields on a validation failure")
    void createValidationFailure() throws Exception {
        String body = """
                {"legalName": "", "registrationNo": "REG-1", "country": "SA",
                 "accountType": "prospect", "source": "api"}
                """;

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors.legalName").exists())
                .andExpect(jsonPath("$.errors.ownerId").exists());

        verify(accountService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/accounts rejects an unknown enum value with 400")
    void createUnknownEnum() throws Exception {
        String body = """
                {"legalName": "Acme LLC", "registrationNo": "REG-1", "country": "SA",
                 "accountType": "nonsense", "ownerId": "%s", "source": "api"}
                """.formatted(ownerId);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).create(any());
    }

    @Test
    @DisplayName("PUT /api/accounts/{id} returns the updated account")
    void update() throws Exception {
        when(accountService.update(eq(accountId), any())).thenReturn(accountDTO(AccountStatus.ACTIVE));

        mockMvc.perform(put("/api/accounts/{id}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDTO(AccountStatus.ACTIVE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()));
    }

    @Test
    @DisplayName("POST /api/accounts/{id}/deactivate returns the deactivated account")
    void deactivate() throws Exception {
        when(accountService.deactivate(accountId)).thenReturn(accountDTO(AccountStatus.INACTIVE));

        mockMvc.perform(post("/api/accounts/{id}/deactivate", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Inactive"));
    }

    @Test
    @DisplayName("DELETE /api/accounts/{id} returns 204")
    void delete204() throws Exception {
        mockMvc.perform(delete("/api/accounts/{id}", accountId))
                .andExpect(status().isNoContent());

        verify(accountService).delete(accountId);
    }

    @Test
    @DisplayName("DELETE /api/accounts/{id} maps a missing account to 404")
    void deleteNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Account not found: " + accountId))
                .when(accountService).delete(accountId);

        mockMvc.perform(delete("/api/accounts/{id}", accountId))
                .andExpect(status().isNotFound());
    }

    private AccountDTO accountDTO(AccountStatus status) {
        return new AccountDTO(
                accountId, "Acme LLC", "Acme", "REG-1", "TAX-1", "Software",
                EmployeeBand.BAND_11_50, "SA", "Riyadh", "https://acme.example",
                null, AccountType.PROSPECT, ownerId, AccountSource.API,
                status, Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"),
                null, null);
    }
}