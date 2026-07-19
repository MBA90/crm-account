package com.crm.account.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Role a contact plays in purchasing decisions.
 */
public enum DecisionRole {

    DECISION_MAKER("decision_maker"),
    INFLUENCER("influencer"),
    TECHNICAL("technical"),
    BILLING("billing");

    private final String value;

    DecisionRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DecisionRole fromValue(String value) {
        for (DecisionRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown decision role: " + value);
    }
}
