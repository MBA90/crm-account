package com.crm.account.dto;

import com.crm.account.domain.enums.ContactStatus;
import com.crm.account.domain.enums.DecisionRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Single transfer object for a contact. Client-supplied fields carry validation;
 * server-managed fields (id and audit timestamps) are populated on responses and
 * ignored on input by {@link com.crm.account.mapper.ContactMapper}.
 */
public record ContactDTO(

        UUID contactId,

        @NotNull
        UUID accountId,

        @NotBlank
        @Size(max = 255)
        String firstName,

        @NotBlank
        @Size(max = 255)
        String lastName,

        @NotBlank
        @Email
        @Size(max = 320)
        String email,

        /** E.164, e.g. +14155552671. */
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "must be a valid E.164 phone number")
        String phone,

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "must be a valid E.164 phone number")
        String mobile,

        @Size(max = 255)
        String jobTitle,

        @Size(max = 255)
        String department,

        boolean isPrimary,

        DecisionRole decisionRole,

        ContactStatus status,

        Instant createdAt,
        Instant updatedAt
) {
}
