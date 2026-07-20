package com.crm.account.web.rest;

import com.crm.account.domain.enums.OwnershipChangeReason;
import com.crm.account.dto.AccountOwnershipHistoryDTO;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.service.AccountOwnershipHistoryService;
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

@WebMvcTest(AccountOwnershipHistoryController.class)
class AccountOwnershipHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountOwnershipHistoryService accountOwnershipHistoryService;

    private final UUID historyId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();
    private final UUID fromOwnerId = UUID.randomUUID();
    private final UUID toOwnerId = UUID.randomUUID();
    private final UUID changedBy = UUID.randomUUID();

    @Test
    @DisplayName("GET /api/account-ownership-history?accountId= returns the trail newest first")
    void listByAccount() throws Exception {
        UUID olderId = UUID.randomUUID();
        when(accountOwnershipHistoryService.getByAccount(accountId))
                .thenReturn(List.of(historyDTO(historyId), historyDTO(olderId)));

        mockMvc.perform(get("/api/account-ownership-history").param("accountId", accountId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].historyId").value(historyId.toString()))
                .andExpect(jsonPath("$[1].historyId").value(olderId.toString()))
                .andExpect(jsonPath("$[0].reason").value("reassignment"));
    }

    @Test
    @DisplayName("GET /api/account-ownership-history without accountId is a 400")
    void listRequiresAccountId() throws Exception {
        mockMvc.perform(get("/api/account-ownership-history"))
                .andExpect(status().isBadRequest());

        verify(accountOwnershipHistoryService, never()).getByAccount(any());
    }

    @Test
    @DisplayName("GET /api/account-ownership-history/{id} returns the entry")
    void getById() throws Exception {
        when(accountOwnershipHistoryService.getById(historyId)).thenReturn(historyDTO(historyId));

        mockMvc.perform(get("/api/account-ownership-history/{id}", historyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.toOwnerId").value(toOwnerId.toString()));
    }

    @Test
    @DisplayName("GET /api/account-ownership-history/{id} maps a missing entry to 404")
    void getByIdNotFound() throws Exception {
        when(accountOwnershipHistoryService.getById(historyId))
                .thenThrow(new ResourceNotFoundException("Ownership history entry not found: " + historyId));

        mockMvc.perform(get("/api/account-ownership-history/{id}", historyId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/account-ownership-history returns 201 with a Location header")
    void create() throws Exception {
        when(accountOwnershipHistoryService.create(any())).thenReturn(historyDTO(historyId));

        mockMvc.perform(post("/api/account-ownership-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(historyDTO(null))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/api/account-ownership-history/" + historyId)));
    }

    @Test
    @DisplayName("POST /api/account-ownership-history accepts a null fromOwnerId for an initial assignment")
    void createAllowsNullFromOwner() throws Exception {
        AccountOwnershipHistoryDTO initial = new AccountOwnershipHistoryDTO(
                null, accountId, null, toOwnerId, OwnershipChangeReason.TERRITORY, changedBy, null);
        when(accountOwnershipHistoryService.create(any())).thenReturn(historyDTO(historyId));

        mockMvc.perform(post("/api/account-ownership-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initial)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/account-ownership-history reports the missing required fields with 400")
    void createValidationFailure() throws Exception {
        mockMvc.perform(post("/api/account-ownership-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.accountId").exists())
                .andExpect(jsonPath("$.errors.toOwnerId").exists())
                .andExpect(jsonPath("$.errors.reason").exists())
                .andExpect(jsonPath("$.errors.changedBy").exists());

        verify(accountOwnershipHistoryService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/account-ownership-history maps a missing account to 404")
    void createUnknownAccount() throws Exception {
        when(accountOwnershipHistoryService.create(any()))
                .thenThrow(new ResourceNotFoundException("Account not found: " + accountId));

        mockMvc.perform(post("/api/account-ownership-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(historyDTO(null))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("the trail is append-only — PUT and DELETE are not mapped")
    void trailIsAppendOnly() throws Exception {
        mockMvc.perform(put("/api/account-ownership-history/{id}", historyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(historyDTO(historyId))))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(delete("/api/account-ownership-history/{id}", historyId))
                .andExpect(status().isMethodNotAllowed());
    }

    private AccountOwnershipHistoryDTO historyDTO(UUID id) {
        return new AccountOwnershipHistoryDTO(
                id, accountId, fromOwnerId, toOwnerId,
                OwnershipChangeReason.REASSIGNMENT, changedBy,
                Instant.parse("2026-01-01T00:00:00Z"));
    }
}