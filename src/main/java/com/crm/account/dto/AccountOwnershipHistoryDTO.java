package com.crm.account.dto;

import com.crm.account.domain.enums.OwnershipChangeReason;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Single transfer object for an ownership history entry. Client-supplied fields
 * carry validation; server-managed fields (id and {@code changedAt}) are populated
 * on responses and ignored on input by
 * {@link com.crm.account.mapper.AccountOwnershipHistoryMapper}.
 */
public record AccountOwnershipHistoryDTO(

        UUID historyId,

        @NotNull
        UUID accountId,

        /** Keycloak subject of the previous owner; null for an initial assignment. */
        UUID fromOwnerId,

        /** Keycloak subject of the new owner. */
        @NotNull
        UUID toOwnerId,

        @NotNull
        OwnershipChangeReason reason,

        /** Keycloak subject of the user who made the change. */
        @NotNull
        UUID changedBy,

        Instant changedAt
) {
}
