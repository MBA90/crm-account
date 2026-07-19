package com.crm.account.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Size band of an account by head count.
 * Stored in the database and exposed over JSON using its label
 * (e.g. {@code "11-50"}) rather than the Java constant name.
 */
public enum EmployeeBand {

    BAND_1_10("1-10"),
    BAND_11_50("11-50"),
    BAND_51_200("51-200"),
    BAND_201_1000("201-1000"),
    BAND_1000_PLUS("1000+");

    private final String value;

    EmployeeBand(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EmployeeBand fromValue(String value) {
        for (EmployeeBand band : values()) {
            if (band.value.equalsIgnoreCase(value)) {
                return band;
            }
        }
        throw new IllegalArgumentException("Unknown employee band: " + value);
    }
}
