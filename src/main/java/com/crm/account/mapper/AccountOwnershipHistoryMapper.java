package com.crm.account.mapper;

import com.crm.account.domain.AccountOwnershipHistory;
import com.crm.account.dto.AccountOwnershipHistoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountOwnershipHistoryMapper {

    @Mapping(target = "historyId", ignore = true)
    @Mapping(target = "changedAt", ignore = true)
    AccountOwnershipHistory toEntity(AccountOwnershipHistoryDTO accountOwnershipHistoryDTO);

    AccountOwnershipHistoryDTO toDTO(AccountOwnershipHistory accountOwnershipHistory);
}
