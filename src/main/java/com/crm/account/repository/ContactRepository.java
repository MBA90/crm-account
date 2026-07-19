package com.crm.account.repository;

import com.crm.account.domain.Contact;
import com.crm.account.domain.enums.ContactStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    /** All contacts of an account. */
    List<Contact> findByAccountId(UUID accountId);

    /** Contacts of an account with a given status (backs idx_contacts_account_id_status). */
    List<Contact> findByAccountIdAndStatus(UUID accountId, ContactStatus status);

    /** Cross-account lookup by email, case-insensitive (backs idx on LOWER(email)). */
    List<Contact> findByEmailIgnoreCase(String email);

    /** The account's primary contact, if any (partial unique guarantees at most one). */
    Optional<Contact> findByAccountIdAndIsPrimaryTrue(UUID accountId);

    /** Enforces UNIQUE (account_id, email) at the application layer. */
    boolean existsByAccountIdAndEmailIgnoreCase(UUID accountId, String email);

    /** Same check excluding a given contact (used on update). */
    boolean existsByAccountIdAndEmailIgnoreCaseAndContactIdNot(UUID accountId, String email, UUID contactId);

    /** Clears the primary flag on all other contacts of an account. */
    @Modifying
    @Query("""
            update Contact c
            set c.isPrimary = false
            where c.accountId = :accountId
              and c.isPrimary = true
              and c.contactId <> :keepContactId
            """)
    void clearPrimaryForOtherContacts(
            @Param("accountId") UUID accountId,
            @Param("keepContactId") UUID keepContactId);
}
