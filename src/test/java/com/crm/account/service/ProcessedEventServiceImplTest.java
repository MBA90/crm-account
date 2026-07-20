package com.crm.account.service;

import com.crm.account.domain.ProcessedEvent;
import com.crm.account.dto.ProcessedEventDTO;
import com.crm.account.exception.DuplicateResourceException;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.mapper.ProcessedEventMapper;
import com.crm.account.repository.ProcessedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessedEventServiceImplTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private ProcessedEventMapper processedEventMapper;

    @InjectMocks
    private ProcessedEventServiceImpl processedEventService;

    private UUID eventId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
    }

    @Test
    @DisplayName("create marks a first-seen event as processed")
    void createMarksEvent() {
        ProcessedEventDTO request = new ProcessedEventDTO(eventId, null);
        ProcessedEvent entity = ProcessedEvent.builder().eventId(eventId).build();
        ProcessedEventDTO stored = new ProcessedEventDTO(eventId, Instant.now());
        when(processedEventRepository.existsById(eventId)).thenReturn(false);
        when(processedEventMapper.toEntity(request)).thenReturn(entity);
        when(processedEventRepository.save(entity)).thenReturn(entity);
        when(processedEventMapper.toDTO(entity)).thenReturn(stored);

        assertThat(processedEventService.create(request)).isEqualTo(stored);
    }

    @Test
    @DisplayName("create signals a replay by throwing on an already-processed event")
    void createRejectsReplay() {
        ProcessedEventDTO request = new ProcessedEventDTO(eventId, null);
        when(processedEventRepository.existsById(eventId)).thenReturn(true);

        assertThatThrownBy(() -> processedEventService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(eventId.toString());

        verify(processedEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("getById returns the marker of a processed event")
    void getById() {
        ProcessedEvent entity = ProcessedEvent.builder().eventId(eventId).build();
        ProcessedEventDTO dto = new ProcessedEventDTO(eventId, Instant.now());
        when(processedEventRepository.findById(eventId)).thenReturn(Optional.of(entity));
        when(processedEventMapper.toDTO(entity)).thenReturn(dto);

        assertThat(processedEventService.getById(eventId)).isEqualTo(dto);
    }

    @Test
    @DisplayName("getById fails for an event that has not been processed")
    void getByIdMissing() {
        when(processedEventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processedEventService.getById(eventId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Processed event not found");
    }
}