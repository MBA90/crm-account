package com.crm.account.service;

import com.crm.account.domain.enums.AccountStatus;
import com.crm.account.dto.AccountDTO;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountDTO create(AccountDTO accountDTO);

    AccountDTO update(UUID accountId, AccountDTO accountDTO);

    AccountDTO getById(UUID accountId);

    List<AccountDTO> getAll();

    List<AccountDTO> getByOwner(UUID ownerId, AccountStatus status);

    /** Sets the account to Inactive and stamps {@code deactivatedAt}. */
    AccountDTO deactivate(UUID accountId);

    /** Soft-deletes the account by stamping {@code erasedAt}. */
    void delete(UUID accountId);
}
