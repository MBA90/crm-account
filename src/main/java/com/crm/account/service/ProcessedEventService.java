package com.crm.account.service;

import com.crm.account.dto.ProcessedEventDTO;

import java.util.UUID;

public interface ProcessedEventService {

    /**
     * Marks an inbound event as processed. Throws
     * {@link com.crm.account.exception.DuplicateResourceException} if the event
     * was already recorded — callers use that signal to skip reprocessing.
     */
    ProcessedEventDTO create(ProcessedEventDTO processedEventDTO);

    ProcessedEventDTO getById(UUID eventId);
}
