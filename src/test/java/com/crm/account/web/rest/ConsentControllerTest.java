package com.crm.account.web.rest;

import com.crm.account.domain.enums.ConsentChannel;
import com.crm.account.domain.enums.ConsentPurpose;
import com.crm.account.domain.enums.ConsentSource;
import com.crm.account.dto.ConsentDTO;
import com.crm.account.exception.DuplicateResourceException;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.service.ConsentService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsentController.class)
class ConsentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConsentService consentService;

    private final UUID consentId = UUID.randomUUID();
    private final UUID contactId = UUID.randomUUID();

    @Test
    @DisplayName("GET /api/consents?contactId= lists the consents of a contact")
    void listByContact() throws Exception {
        when(consentService.getByContact(contactId)).thenReturn(List.of(consentDTO(true, null)));

        mockMvc.perform(get("/api/consents").param("contactId", contactId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].channel").value("email"))
                .andExpect(jsonPath("$[0].purpose").value("marketing"))
                .andExpect(jsonPath("$[0].source").value("web_form"))
                .andExpect(jsonPath("$[0].granted").value(true));
    }

    @Test
    @DisplayName("GET /api/consents without contactId is a 400 — the filter is required")
    void listRequiresContactId() throws Exception {
        mockMvc.perform(get("/api/consents"))
                .andExpect(status().isBadRequest());

        verify(consentService, never()).getByContact(any());
    }

    @Test
    @DisplayName("GET /api/consents/{id} returns the consent")
    void getById() throws Exception {
        when(consentService.getById(consentId)).thenReturn(consentDTO(true, null));

        mockMvc.perform(get("/api/consents/{id}", consentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consentId").value(consentId.toString()));
    }

    @Test
    @DisplayName("GET /api/consents/{id} maps a missing consent to 404")
    void getByIdNotFound() throws Exception {
        when(consentService.getById(consentId))
                .thenThrow(new ResourceNotFoundException("Consent not found: " + consentId));

        mockMvc.perform(get("/api/consents/{id}", consentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/consents returns 201 with a Location header")
    void create() throws Exception {
        when(consentService.create(any())).thenReturn(consentDTO(true, null));

        mockMvc.perform(post("/api/consents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consentDTO(true, null))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/api/consents/" + consentId)));
    }

    @Test
    @DisplayName("POST /api/consents maps a duplicate channel/purpose to 409")
    void createDuplicate() throws Exception {
        when(consentService.create(any())).thenThrow(new DuplicateResourceException("already exists"));

        mockMvc.perform(post("/api/consents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consentDTO(true, null))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/consents reports the missing required fields with 400")
    void createValidationFailure() throws Exception {
        mockMvc.perform(post("/api/consents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"granted\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.contactId").exists())
                .andExpect(jsonPath("$.errors.channel").exists())
                .andExpect(jsonPath("$.errors.purpose").exists())
                .andExpect(jsonPath("$.errors.source").exists());

        verify(consentService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/consents/{id}/withdraw returns the withdrawn consent")
    void withdraw() throws Exception {
        Instant withdrawnAt = Instant.parse("2026-02-01T00:00:00Z");
        when(consentService.withdraw(consentId)).thenReturn(consentDTO(false, withdrawnAt));

        mockMvc.perform(post("/api/consents/{id}/withdraw", consentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(false))
                .andExpect(jsonPath("$.withdrawnAt").exists());
    }

    @Test
    @DisplayName("POST /api/consents/{id}/withdraw maps a missing consent to 404")
    void withdrawNotFound() throws Exception {
        when(consentService.withdraw(consentId))
                .thenThrow(new ResourceNotFoundException("Consent not found: " + consentId));

        mockMvc.perform(post("/api/consents/{id}/withdraw", consentId))
                .andExpect(status().isNotFound());
    }

    private ConsentDTO consentDTO(boolean granted, Instant withdrawnAt) {
        return new ConsentDTO(
                consentId, contactId, ConsentChannel.EMAIL, ConsentPurpose.MARKETING,
                granted, ConsentSource.WEB_FORM, null,
                Instant.parse("2026-01-01T00:00:00Z"), withdrawnAt);
    }
}