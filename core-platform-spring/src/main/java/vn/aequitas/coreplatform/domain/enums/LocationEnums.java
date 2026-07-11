package vn.aequitas.coreplatform.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Location code. Only used in salary calculation DTOs (never persisted).
 */
public enum LocationEnums {
    None(0),
    VLB(1),
    TC(2);

    private final int value;

    LocationEnums(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static LocationEnums fromValue(int value) {
        for (LocationEnums e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid LocationEnums value: " + value);
    }

    @JsonCreator
    public static LocationEnums fromJson(Object value) {
        return EnumJson.parse(LocationEnums.class, values(), value, LocationEnums::fromValue);
    }
}
