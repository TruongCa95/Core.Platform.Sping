package vn.aequitas.coreplatform.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * School level. Persisted as its numeric value (see {@code LevelEnumsConverter}),
 * which is NOT the ordinal - {@code Other} is 99 - so an attribute converter is
 * required to stay compatible with the existing column data.
 */
public enum LevelEnums {
    PrimarySchool(1),
    SecondarySchool(2),
    HighSchool(3),
    Other(99);

    private final int value;

    LevelEnums(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static LevelEnums fromValue(int value) {
        for (LevelEnums e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid LevelEnums value: " + value);
    }

    @JsonCreator
    public static LevelEnums fromJson(Object value) {
        return EnumJson.parse(LevelEnums.class, values(), value, LevelEnums::fromValue);
    }
}
