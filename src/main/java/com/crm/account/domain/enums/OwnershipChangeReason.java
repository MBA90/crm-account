package com.crm.account.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Why an account changed owner.
 */
public enum OwnershipChangeReason {

    REASSIGNMENT("reassignment"),
    DEPARTURE("departure"),
    TERRITORY("territory");

    private final String value;

    OwnershipChangeReason(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static OwnershipChangeReason fromValue(String value) {
        for (OwnershipChangeReason reason : values()) {
            if (reason.value.equalsIgnoreCase(value)) {
                return reason;
            }
        }
        throw new IllegalArgumentException("Unknown ownership change reason: " + value);
    }
}
