package com.crm.account.domain;

import com.crm.account.domain.enums.AccountSource;
import com.crm.account.domain.enums.AccountStatus;
import com.crm.account.domain.enums.AccountType;
import com.crm.account.domain.enums.EmployeeBand;
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
 * A business account (company) tracked by the CRM.
 */
@Entity
@Table(
        name = "accounts",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_accounts_registration_no_country",
                columnNames = {"registration_no", "country"}
        ),
        indexes = {
                @Index(name = "idx_accounts_parent_account_id", columnList = "parent_account_id"),
                @Index(name = "idx_accounts_owner_id_status", columnList = "owner_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    /** Primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "account_id", updatable = false, nullable = false)
    private UUID accountId;

    /** Legal name as registered. */
    @Column(name = "legal_name", nullable = false)
    private String legalName;

    /** Trade name ("doing business as"), optional. */
    @Column(name = "trade_name")
    private String tradeName;

    /** Commercial registration / company number. */
    @Column(name = "registration_no", nullable = false)
    private String registrationNo;

    /** VAT / tax identifier. */
    @Column(name = "tax_id")
    private String taxId;

    /** Industry classification. */
    @Column(name = "industry")
    private String industry;

    /** Head-count band. */
    @Column(name = "employee_band")
    private EmployeeBand employeeBand;

    /** Country (part of the registration uniqueness key). */
    @Column(name = "country")
    private String country;

    /** City. */
    @Column(name = "city")
    private String city;

    /** Public website, optional. */
    @Column(name = "website")
    private String website;

    /** Parent account (self reference), optional. */
    @Column(name = "parent_account_id")
    private UUID parentAccountId;

    /** Commercial relationship type. */
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    /** Keycloak subject of the owning sales rep. */
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    /** Channel the record originated from. */
    @Column(name = "source", nullable = false)
    private AccountSource source;

    /** Lifecycle status. */
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** When the account was last deactivated, optional. */
    @Column(name = "deactivated_at")
    private Instant deactivatedAt;

    /** When the account was erased (soft delete / GDPR), optional. */
    @Column(name = "erased_at")
    private Instant erasedAt;
}
