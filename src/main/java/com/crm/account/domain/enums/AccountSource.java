package com.crm.account.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Origin channel through which the account record was created.
 */
public enum AccountSource {

    ADMIN_PANEL("admin_panel"),
    IMPORT("import"),
    API("api");

    private final String value;

    AccountSource(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AccountSource fromValue(String value) {
        for (AccountSource source : values()) {
            if (source.value.equalsIgnoreCase(value)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown account source: " + value);
    }
}
