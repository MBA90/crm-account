package com.crm.account.mapper;

import com.crm.account.domain.Account;
import com.crm.account.dto.AccountDTO;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deactivatedAt", ignore = true)
    @Mapping(target = "erasedAt", ignore = true)
    Account toEntity(AccountDTO accountDTO);

    AccountDTO toDTO(Account account);
}
