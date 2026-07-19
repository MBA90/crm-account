package com.crm.account.domain;

import com.crm.account.domain.enums.ContactStatus;
import com.crm.account.domain.enums.DecisionRole;
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
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * A person associated with an account.
 *
 * <p>Email is intentionally <strong>not</strong> globally unique — the same person
 * can be a contact at several accounts. Uniqueness is scoped to
 * {@code (account_id, email)}. The functional {@code LOWER(email)} lookup index and
 * the partial "one primary per account" unique index are defined in Liquibase.
 */
@Entity
@Table(
        name = "contacts",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_contacts_account_id_email",
                columnNames = {"account_id", "email"}
        ),
        indexes = {
                @Index(name = "idx_contacts_account_id_status", columnList = "account_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {

    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "contact_id", updatable = false, nullable = false)
    private UUID contactId;

    /** Owning account (FK to accounts). */
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    /** Unique within the account (see class docs). */
    @Column(name = "email", nullable = false)
    private String email;

    /** Landline in E.164 form, optional. */
    @Column(name = "phone")
    private String phone;

    /** Mobile in E.164 form, optional. */
    @Column(name = "mobile")
    private String mobile;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "department")
    private String department;

    /** Whether this is the account's primary contact (at most one per account). */
    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    /** Role in purchasing decisions. */
    @Column(name = "decision_role")
    private DecisionRole decisionRole;

    /** Lifecycle status. */
    @Column(name = "status", nullable = false)
    private ContactStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
