package com.crm.account.dto;

import com.crm.account.domain.enums.ConsentChannel;
import com.crm.account.domain.enums.ConsentPurpose;
import com.crm.account.domain.enums.ConsentSource;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Single transfer object for a consent. Client-supplied fields carry validation;
 * server-managed fields (id, {@code grantedAt} and {@code withdrawnAt}) are
 * populated on responses and ignored on input by
 * {@link com.crm.account.mapper.ConsentMapper}.
 */
public record ConsentDTO(

        UUID consentId,

        @NotNull
        UUID contactId,

        @NotNull
        ConsentChannel channel,

        @NotNull
        ConsentPurpose purpose,

        boolean granted,

        @NotNull
        ConsentSource source,

        /** Reference to an evidence document held in another service, optional. */
        UUID evidenceRef,

        Instant grantedAt,
        Instant withdrawnAt
) {
}
