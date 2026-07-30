package com.crm.account.service;

import com.crm.account.dto.AccountDTO;
import com.crm.account.dto.AccountSearchCriteriaDTO;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface AccountService {

    AccountDTO create(AccountDTO accountDTO);

    AccountDTO update(UUID accountId, AccountDTO accountDTO);

    AccountDTO getById(UUID accountId);

    /** Filter fields on criteria are optional (null/blank is ignored); pagination defaults if omitted. */
    Page<AccountDTO> search(AccountSearchCriteriaDTO criteria);

    /** Sets the account to Inactive and stamps {@code deactivatedAt}. */
    AccountDTO deactivate(UUID accountId);

    /** Soft-deletes the account by stamping {@code erasedAt}. */
    void delete(UUID accountId);
}
