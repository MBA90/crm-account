package com.crm.account.web.rest;

import com.crm.account.domain.enums.ContactStatus;
import com.crm.account.domain.enums.DecisionRole;
import com.crm.account.dto.ContactDTO;
import com.crm.account.exception.DuplicateResourceException;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.service.ContactService;
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

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContactService contactService;

    private final UUID contactId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();

    @Test
    @DisplayName("GET /api/contacts?accountId= lists the contacts of an account")
    void listByAccount() throws Exception {
        when(contactService.getByAccount(accountId, null)).thenReturn(List.of(contactDTO()));

        mockMvc.perform(get("/api/contacts").param("accountId", accountId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("jane@acme.example"))
                .andExpect(jsonPath("$[0].decisionRole").value("influencer"))
                .andExpect(jsonPath("$[0].status").value("Active"));
    }

    @Test
    @DisplayName("GET /api/contacts?accountId=&status= passes the status filter through")
    void listByAccountAndStatus() throws Exception {
        when(contactService.getByAccount(accountId, ContactStatus.INACTIVE)).thenReturn(List.of());

        mockMvc.perform(get("/api/contacts")
                        .param("accountId", accountId.toString())
                        .param("status", "INACTIVE"))
                .andExpect(status().isOk());

        verify(contactService).getByAccount(accountId, ContactStatus.INACTIVE);
    }

    @Test
    @DisplayName("GET /api/contacts?email= takes precedence over the account filter")
    void listByEmailWins() throws Exception {
        when(contactService.getByEmail("jane@acme.example")).thenReturn(List.of(contactDTO()));

        mockMvc.perform(get("/api/contacts")
                        .param("email", "jane@acme.example")
                        .param("accountId", accountId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contactId").value(contactId.toString()));

        verify(contactService).getByEmail("jane@acme.example");
        verify(contactService, never()).getByAccount(any(), any());
    }

    @Test
    @DisplayName("GET /api/contacts/{id} returns the contact")
    void getById() throws Exception {
        when(contactService.getById(contactId)).thenReturn(contactDTO());

        mockMvc.perform(get("/api/contacts/{id}", contactId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.isPrimary").value(true));
    }

    @Test
    @DisplayName("GET /api/contacts/{id} maps a missing contact to 404")
    void getByIdNotFound() throws Exception {
        when(contactService.getById(contactId))
                .thenThrow(new ResourceNotFoundException("Contact not found: " + contactId));

        mockMvc.perform(get("/api/contacts/{id}", contactId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/contacts returns 201 with a Location header")
    void create() throws Exception {
        when(contactService.create(any())).thenReturn(contactDTO());

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contactDTO())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/api/contacts/" + contactId)));
    }

    @Test
    @DisplayName("POST /api/contacts maps a duplicate email to 409")
    void createDuplicate() throws Exception {
        when(contactService.create(any())).thenThrow(new DuplicateResourceException("already exists"));

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contactDTO())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/contacts rejects a malformed email with 400")
    void createInvalidEmail() throws Exception {
        String body = """
                {"accountId": "%s", "firstName": "Jane", "lastName": "Doe",
                 "email": "not-an-email"}
                """.formatted(accountId);

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());

        verify(contactService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/contacts rejects a phone that is not E.164 with 400")
    void createInvalidPhone() throws Exception {
        String body = """
                {"accountId": "%s", "firstName": "Jane", "lastName": "Doe",
                 "email": "jane@acme.example", "phone": "0555 not a number"}
                """.formatted(accountId);

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.phone").value("must be a valid E.164 phone number"));
    }

    @Test
    @DisplayName("PUT /api/contacts/{id} returns the updated contact")
    void update() throws Exception {
        when(contactService.update(eq(contactId), any())).thenReturn(contactDTO());

        mockMvc.perform(put("/api/contacts/{id}", contactId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contactDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactId").value(contactId.toString()));
    }

    @Test
    @DisplayName("DELETE /api/contacts/{id} returns 204")
    void delete204() throws Exception {
        mockMvc.perform(delete("/api/contacts/{id}", contactId))
                .andExpect(status().isNoContent());

        verify(contactService).delete(contactId);
    }

    private ContactDTO contactDTO() {
        return new ContactDTO(
                contactId, accountId, "Jane", "Doe", "jane@acme.example",
                "+14155552671", null, "CFO", "Finance",
                true, DecisionRole.INFLUENCER, ContactStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));
    }
}