package com.crm.account.mapper;

import com.crm.account.domain.ProcessedEvent;
import com.crm.account.dto.ProcessedEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProcessedEventMapper {

    /** eventId is intentionally mapped — it is the inbound event's own id, not generated. */
    @Mapping(target = "processedAt", ignore = true)
    ProcessedEvent toEntity(ProcessedEventDTO processedEventDTO);

    ProcessedEventDTO toDTO(ProcessedEvent processedEvent);
}
