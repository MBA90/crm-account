package com.crm.account.domain;

import com.crm.account.domain.enums.OwnershipChangeReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Append-only record of account owner (sales rep / territory) changes.
 *
 * <p>Rows are never updated or deleted — reps leave and territories get redrawn,
 * and this trail keeps commission disputes and pipeline attribution answerable.
 * Recording an entry does <strong>not</strong> itself mutate {@code accounts.owner_id}.
 */
@Entity
@Table(
        name = "account_ownership_history",
        indexes = {
                @Index(name = "idx_account_ownership_history_account_id_changed_at",
                        columnList = "account_id, changed_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountOwnershipHistory {

    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id", updatable = false, nullable = false)
    private UUID historyId;

    /** Account whose ownership changed (FK to accounts). */
    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    /** Keycloak subject of the previous owner; null for an initial assignment. */
    @Column(name = "from_owner_id", updatable = false)
    private UUID fromOwnerId;

    /** Keycloak subject of the new owner. */
    @Column(name = "to_owner_id", nullable = false, updatable = false)
    private UUID toOwnerId;

    /** Why the ownership changed. */
    @Column(name = "reason", nullable = false, updatable = false)
    private OwnershipChangeReason reason;

    /** Keycloak subject of the user who made the change. */
    @Column(name = "changed_by", nullable = false, updatable = false)
    private UUID changedBy;

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;
}
