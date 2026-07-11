package vn.aequitas.coreplatform.application.common.validation;

import java.util.Collection;
import java.util.UUID;

/**
 * Reusable predicates backing the fluent rule methods. Semantics mirror the
 * corresponding FluentValidation built-ins used by the .NET validators.
 */
final class Rules {

    static final UUID EMPTY_UUID = new UUID(0L, 0L);

    private Rules() {
    }

    /**
     * FluentValidation {@code NotEmpty}: rejects null, blank strings, empty
     * collections and the all-zero GUID; a present temporal/number is accepted.
     */
    static boolean notEmpty(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof CharSequence cs) {
            return !cs.toString().trim().isEmpty();
        }
        if (value instanceof Collection<?> c) {
            return !c.isEmpty();
        }
        if (value instanceof UUID id) {
            return !id.equals(EMPTY_UUID);
        }
        return true;
    }

    static boolean maximumLength(Object value, int max) {
        // Null passes: length limits only constrain supplied values (matches FluentValidation).
        return value == null || value.toString().length() <= max;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static boolean greaterThan(Object value, Comparable<?> bound) {
        if (value == null) {
            return false;
        }
        return ((Comparable) value).compareTo(bound) > 0;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static boolean greaterThanOrEqualTo(Object value, Comparable<?> bound) {
        if (value == null) {
            return false;
        }
        return ((Comparable) value).compareTo(bound) >= 0;
    }

    /**
     * FluentValidation {@code IsInEnum}: a typed enum value is always a defined
     * member, so this reduces to a not-null check (invalid names are already
     * rejected by JSON binding before validation runs).
     */
    static boolean isInEnum(Object value) {
        return value instanceof Enum<?>;
    }
}
