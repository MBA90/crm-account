package com.crm.account.domain;

import com.crm.account.domain.enums.ConsentChannel;
import com.crm.account.domain.enums.ConsentPurpose;
import com.crm.account.domain.enums.ConsentSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * A communication consent, attached to the person (contact), not the company.
 *
 * <p>One consent per {@code (contact_id, channel, purpose)}. A consent is never
 * edited or deleted — it is legal evidence; changing a preference means
 * withdrawing the existing consent and granting a new one.
 */
@Entity
@Table(
        name = "consents",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_consents_contact_id_channel_purpose",
                columnNames = {"contact_id", "channel", "purpose"}
        ),
        indexes = {
                @Index(name = "idx_consents_contact_id", columnList = "contact_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consent {

    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "consent_id", updatable = false, nullable = false)
    private UUID consentId;

    /** Person the consent belongs to (FK to contacts). */
    @Column(name = "contact_id", nullable = false, updatable = false)
    private UUID contactId;

    /** Channel the consent applies to. */
    @Column(name = "channel", nullable = false, updatable = false)
    private ConsentChannel channel;

    /** Purpose the consent covers. */
    @Column(name = "purpose", nullable = false, updatable = false)
    private ConsentPurpose purpose;

    /** Whether the consent is currently granted; set to false on withdrawal. */
    @Column(name = "granted", nullable = false)
    private boolean granted;

    /** How the consent was captured. */
    @Column(name = "source", nullable = false, updatable = false)
    private ConsentSource source;

    /**
     * Reference to an evidence document (workflow attachment in another service).
     * Stored as a plain UUID — the referenced table lives outside this schema.
     */
    @Column(name = "evidence_ref", updatable = false)
    private UUID evidenceRef;

    @CreationTimestamp
    @Column(name = "granted_at", nullable = false, updatable = false)
    private Instant grantedAt;

    /** When the consent was withdrawn, if ever. */
    @Column(name = "withdrawn_at")
    private Instant withdrawnAt;
}
