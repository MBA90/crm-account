package com.crm.account.service;

import com.crm.account.domain.ProcessedEvent;
import com.crm.account.dto.ProcessedEventDTO;
import com.crm.account.exception.DuplicateResourceException;
import com.crm.account.exception.ResourceNotFoundException;
import com.crm.account.mapper.ProcessedEventMapper;
import com.crm.account.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProcessedEventServiceImpl implements ProcessedEventService {

    private final ProcessedEventRepository processedEventRepository;
    private final ProcessedEventMapper processedEventMapper;

    @Override
    public ProcessedEventDTO create(ProcessedEventDTO processedEventDTO) {
        if (processedEventRepository.existsById(processedEventDTO.eventId())) {
            throw new DuplicateResourceException(
                    "Event '%s' has already been processed".formatted(processedEventDTO.eventId()));
        }

        ProcessedEvent processedEvent = processedEventMapper.toEntity(processedEventDTO);
        return processedEventMapper.toDTO(processedEventRepository.save(processedEvent));
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessedEventDTO getById(UUID eventId) {
        return processedEventRepository.findById(eventId)
                .map(processedEventMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Processed event not found: " + eventId));
    }
}
