package com.crm.account.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * How a consent was captured.
 */
public enum ConsentSource {

    CONTRACT("contract"),
    WEB_FORM("web_form"),
    VERBAL_LOGGED("verbal_logged");

    private final String value;

    ConsentSource(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ConsentSource fromValue(String value) {
        for (ConsentSource source : values()) {
            if (source.value.equalsIgnoreCase(value)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown consent source: " + value);
    }
}
