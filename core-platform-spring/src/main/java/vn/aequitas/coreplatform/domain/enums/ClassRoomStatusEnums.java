package vn.aequitas.coreplatform.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Classroom lifecycle status. Persisted as its numeric value
 * (see {@code ClassRoomStatusEnumsConverter}).
 */
public enum ClassRoomStatusEnums {
    Active(0),      // Hoạt động
    Paused(1),      // Tạm dừng
    Inactive(2);    // Ngừng hoạt động

    private final int value;

    ClassRoomStatusEnums(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ClassRoomStatusEnums fromValue(int value) {
        for (ClassRoomStatusEnums e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid ClassRoomStatusEnums value: " + value);
    }

    @JsonCreator
    public static ClassRoomStatusEnums fromJson(Object value) {
        return EnumJson.parse(ClassRoomStatusEnums.class, values(), value, ClassRoomStatusEnums::fromValue);
    }
}
