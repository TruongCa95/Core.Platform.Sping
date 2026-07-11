package vn.aequitas.coreplatform.domain.enums;

import java.util.function.IntFunction;

/**
 * Small helper shared by the enum {@code @JsonCreator} methods so a JSON value
 * can be supplied either as the constant name (case-insensitive, e.g. "APlus")
 * or as the underlying numeric value, matching the behaviour of the .NET
 * {@code JsonStringEnumConverter}.
 */
final class EnumJson {

    private EnumJson() {
    }

    static <E extends Enum<E>> E parse(Class<E> type, E[] values, Object json, IntFunction<E> fromValue) {
        if (json == null) {
            return null;
        }
        if (json instanceof Number number) {
            return fromValue.apply(number.intValue());
        }
        String text = json.toString().trim();
        for (E value : values) {
            if (value.name().equalsIgnoreCase(text)) {
                return value;
            }
        }
        try {
            return fromValue.apply(Integer.parseInt(text));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + type.getSimpleName() + " value: " + text);
        }
    }
}
