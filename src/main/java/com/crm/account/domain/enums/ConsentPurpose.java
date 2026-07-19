package com.crm.account.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Purpose a consent covers. Transactional contact is generally lawful without
 * consent; marketing is not — keep them as separate purposes so operational
 * email is not over-blocked.
 */
public enum ConsentPurpose {

    MARKETING("marketing"),
    TRANSACTIONAL("transactional"),
    PROFILING("profiling");

    private final String value;

    ConsentPurpose(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ConsentPurpose fromValue(String value) {
        for (ConsentPurpose purpose : values()) {
            if (purpose.value.equalsIgnoreCase(value)) {
                return purpose;
            }
        }
        throw new IllegalArgumentException("Unknown consent purpose: " + value);
    }
}
