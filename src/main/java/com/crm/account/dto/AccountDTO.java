package com.crm.account.dto;

import com.crm.account.domain.enums.AccountSource;
import com.crm.account.domain.enums.AccountStatus;
import com.crm.account.domain.enums.AccountType;
import com.crm.account.domain.enums.EmployeeBand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Single transfer object for an account. Client-supplied fields carry validation;
 * server-managed fields (id, audit and lifecycle timestamps) are populated on
 * responses and ignored on input by {@link com.crm.account.mapper.AccountMapper}.
 */
public record AccountDTO(

        UUID accountId,

        @NotBlank
        @Size(max = 255)
        String legalName,

        @Size(max = 255)
        String tradeName,

        @NotBlank
        @Size(max = 100)
        String registrationNo,

        @Size(max = 100)
        String taxId,

        @Size(max = 255)
        String industry,

        EmployeeBand employeeBand,

        @NotBlank
        @Size(max = 100)
        String country,

        @Size(max = 100)
        String city,

        @Size(max = 255)
        String website,

        UUID parentAccountId,

        @NotNull
        AccountType accountType,

        @NotNull
        UUID ownerId,

        @NotNull
        AccountSource source,

        AccountStatus status,

        Instant createdAt,
        Instant updatedAt,
        Instant deactivatedAt,
        Instant erasedAt
) {
}
