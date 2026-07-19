package com.crm.account.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Lifecycle status of a contact.
 */
public enum ContactStatus {

    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String value;

    ContactStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ContactStatus fromValue(String value) {
        for (ContactStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown contact status: " + value);
    }
}
