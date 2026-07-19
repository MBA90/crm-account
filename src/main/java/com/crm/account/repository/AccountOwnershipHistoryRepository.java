package com.crm.account.repository;

import com.crm.account.domain.AccountOwnershipHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountOwnershipHistoryRepository extends JpaRepository<AccountOwnershipHistory, UUID> {

    /** Ownership trail of an account, newest first (backs idx_account_ownership_history_account_id_changed_at). */
    List<AccountOwnershipHistory> findByAccountIdOrderByChangedAtDesc(UUID accountId);
}
