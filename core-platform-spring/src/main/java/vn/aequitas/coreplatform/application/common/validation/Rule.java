package vn.aequitas.coreplatform.application.common.validation;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * One property rule: selects a value, tests it and records a failure with a
 * (possibly overridden) message. Analogous to a single FluentValidation validator
 * attached to a {@code RuleFor} chain.
 */
class Rule<T> {

    private final String propertyName;
    private final Function<T, Object> selector;
    private final Predicate<Object> isValid;
    private String message;

    Rule(String propertyName, Function<T, Object> selector, Predicate<Object> isValid, String message) {
        this.propertyName = propertyName;
        this.selector = selector;
        this.isValid = isValid;
        this.message = message;
    }

    void overrideMessage(String newMessage) {
        this.message = newMessage;
    }

    void evaluate(T instance, List<ValidationFailure> failures) {
        Object value = selector.apply(instance);
        if (!isValid.test(value)) {
            failures.add(new ValidationFailure(propertyName, message));
        }
    }
}
