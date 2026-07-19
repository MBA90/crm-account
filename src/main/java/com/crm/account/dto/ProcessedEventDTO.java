package com.crm.account.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Single transfer object for a processed-event marker. {@code eventId} is the
 * id of the inbound event and is client-supplied; {@code processedAt} is
 * server-managed and ignored on input by
 * {@link com.crm.account.mapper.ProcessedEventMapper}.
 */
public record ProcessedEventDTO(

        @NotNull
        UUID eventId,

        Instant processedAt
) {
}
