package com.crm.account.service;

import com.crm.account.dto.AccountOwnershipHistoryDTO;

import java.util.List;
import java.util.UUID;

public interface AccountOwnershipHistoryService {

    /**
     * Records an ownership change. Append-only: entries are never updated or
     * deleted, and recording one does not mutate {@code accounts.owner_id}.
     */
    AccountOwnershipHistoryDTO create(AccountOwnershipHistoryDTO accountOwnershipHistoryDTO);

    AccountOwnershipHistoryDTO getById(UUID historyId);

    /** Ownership trail of an account, newest first. */
    List<AccountOwnershipHistoryDTO> getByAccount(UUID accountId);
}
