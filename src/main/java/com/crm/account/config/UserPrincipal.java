package com.crm.account.config;

import java.util.UUID;

public record UserPrincipal(
        UUID userId,
        String username,
        String email,
        String name,
        String department,
        String phone,
        String mobile
) {
}
