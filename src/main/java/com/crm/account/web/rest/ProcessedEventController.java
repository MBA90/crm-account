package com.crm.account.web.rest;

import com.crm.account.dto.ProcessedEventDTO;
import com.crm.account.service.ProcessedEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * Idempotency guard: POST marks an event as processed (409 if it already was),
 * GET checks whether it has been. Markers are never updated or deleted.
 */
@RestController
@RequestMapping("/api/processed-events")
@RequiredArgsConstructor
public class ProcessedEventController {

    private final ProcessedEventService processedEventService;

    @GetMapping("/{eventId}")
    public ProcessedEventDTO getById(@PathVariable UUID eventId) {
        return processedEventService.getById(eventId);
    }

    @PostMapping
    public ResponseEntity<ProcessedEventDTO> create(
            @Valid @RequestBody ProcessedEventDTO processedEventDTO,
            UriComponentsBuilder uriBuilder) {
        ProcessedEventDTO created = processedEventService.create(processedEventDTO);
        URI location = uriBuilder.path("/api/processed-events/{eventId}")
                .buildAndExpand(created.eventId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
