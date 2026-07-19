package com.crm.account.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Communication channel a consent applies to.
 */
public enum ConsentChannel {

    EMAIL("email"),
    SMS("sms"),
    PHONE("phone"),
    POSTAL("postal");

    private final String value;

    ConsentChannel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ConsentChannel fromValue(String value) {
        for (ConsentChannel channel : values()) {
            if (channel.value.equalsIgnoreCase(value)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unknown consent channel: " + value);
    }
}
