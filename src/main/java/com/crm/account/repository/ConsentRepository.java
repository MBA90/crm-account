package com.crm.account.repository;

import com.crm.account.domain.Consent;
import com.crm.account.domain.enums.ConsentChannel;
import com.crm.account.domain.enums.ConsentPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, UUID> {

    /** All consents of a contact. */
    List<Consent> findByContactId(UUID contactId);

    /** Enforces UNIQUE (contact_id, channel, purpose) at the application layer. */
    boolean existsByContactIdAndChannelAndPurpose(UUID contactId, ConsentChannel channel, ConsentPurpose purpose);
}
