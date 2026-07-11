package vn.aequitas.coreplatform.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Teaching-quality grade. Only used in salary calculation DTOs (never persisted),
 * so no JPA converter is needed. Numeric values preserved from the .NET enum.
 */
public enum KiEnums {
    APlus(0),
    A(1),
    B(2),
    BPlus(3),
    C(4),
    D(5);

    private final int value;

    KiEnums(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static KiEnums fromValue(int value) {
        for (KiEnums e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid KiEnums value: " + value);
    }

    /** Accept either the constant name (e.g. "APlus") or the numeric value, matching the .NET JsonStringEnumConverter. */
    @JsonCreator
    public static KiEnums fromJson(Object value) {
        return EnumJson.parse(KiEnums.class, values(), value, KiEnums::fromValue);
    }
}
