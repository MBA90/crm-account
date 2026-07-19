package com.crm.account.mapper;

import com.crm.account.domain.Consent;
import com.crm.account.dto.ConsentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConsentMapper {

    @Mapping(target = "consentId", ignore = true)
    @Mapping(target = "grantedAt", ignore = true)
    @Mapping(target = "withdrawnAt", ignore = true)
    Consent toEntity(ConsentDTO consentDTO);

    ConsentDTO toDTO(Consent consent);
}
