package com.crm.account.web.rest;

import com.crm.account.dto.ProcessedEventDTO;
import com.crm.account.exception.DuplicateResourceException;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.service.ProcessedEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcessedEventController.class)
class ProcessedEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcessedEventService processedEventService;

    private final UUID eventId = UUID.randomUUID();

    @Test
    @DisplayName("GET /api/processed-events/{id} confirms an event was processed")
    void getById() throws Exception {
        when(processedEventService.getById(eventId)).thenReturn(processedEventDTO());

        mockMvc.perform(get("/api/processed-events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(eventId.toString()))
                .andExpect(jsonPath("$.processedAt").exists());
    }

    @Test
    @DisplayName("GET /api/processed-events/{id} is a 404 for an unseen event")
    void getByIdNotFound() throws Exception {
        when(processedEventService.getById(eventId))
                .thenThrow(new ResourceNotFoundException("Processed event not found: " + eventId));

        mockMvc.perform(get("/api/processed-events/{id}", eventId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/processed-events returns 201 with a Location header")
    void create() throws Exception {
        when(processedEventService.create(any())).thenReturn(processedEventDTO());

        mockMvc.perform(post("/api/processed-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProcessedEventDTO(eventId, null))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/api/processed-events/" + eventId)));
    }

    @Test
    @DisplayName("POST /api/processed-events returns 409 on a replay — the idempotency signal")
    void createReplay() throws Exception {
        when(processedEventService.create(any()))
                .thenThrow(new DuplicateResourceException("Event '%s' has already been processed".formatted(eventId)));

        mockMvc.perform(post("/api/processed-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProcessedEventDTO(eventId, null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Event '%s' has already been processed".formatted(eventId)));
    }

    @Test
    @DisplayName("POST /api/processed-events requires an eventId")
    void createRequiresEventId() throws Exception {
        mockMvc.perform(post("/api/processed-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.eventId").exists());

        verify(processedEventService, never()).create(any());
    }

    @Test
    @DisplayName("markers are never deleted — DELETE is not mapped")
    void markersAreNotDeletable() throws Exception {
        mockMvc.perform(delete("/api/processed-events/{id}", eventId))
                .andExpect(status().isMethodNotAllowed());
    }

    private ProcessedEventDTO processedEventDTO() {
        return new ProcessedEventDTO(eventId, Instant.parse("2026-01-01T00:00:00Z"));
    }
}