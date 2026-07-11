package vn.aequitas.coreplatform.application.common.validation;

import java.util.function.Function;

/**
 * Fluent builder for the rules attached to one property, mirroring the chained
 * FluentValidation API ({@code RuleFor(x => x.Prop).NotEmpty().MaximumLength(50)}).
 * Each rule method appends a {@link Rule} with a default message; {@link #withMessage}
 * overrides the message of the most recently added rule, exactly like FluentValidation.
 *
 * @param <T> the validated type
 * @param <P> the selected property type
 */
public class RuleBuilder<T, P> {

    private final AbstractValidator<T> owner;
    private final String propertyName;
    private final Function<T, P> selector;
    private Rule<T> lastRule;

    RuleBuilder(AbstractValidator<T> owner, String propertyName, Function<T, P> selector) {
        this.owner = owner;
        this.propertyName = propertyName;
        this.selector = selector;
    }

    private RuleBuilder<T, P> addRule(java.util.function.Predicate<Object> isValid, String defaultMessage) {
        // Widen the selector's return type to Object (P is assignable to Object) without an unchecked cast.
        Function<T, Object> widened = instance -> selector.apply(instance);
        Rule<T> rule = new Rule<>(propertyName, widened, isValid, defaultMessage);
        owner.register(rule);
        this.lastRule = rule;
        return this;
    }

    public RuleBuilder<T, P> notEmpty() {
        return addRule(Rules::notEmpty, propertyName + " must not be empty.");
    }

    public RuleBuilder<T, P> notNull() {
        return addRule(value -> value != null, propertyName + " must not be null.");
    }

    public RuleBuilder<T, P> maximumLength(int max) {
        return addRule(value -> Rules.maximumLength(value, max),
                propertyName + " cannot exceed " + max + " characters.");
    }

    public RuleBuilder<T, P> greaterThan(Comparable<?> bound) {
        return addRule(value -> Rules.greaterThan(value, bound),
                propertyName + " must be greater than " + bound + ".");
    }

    public RuleBuilder<T, P> greaterThanOrEqualTo(Comparable<?> bound) {
        return addRule(value -> Rules.greaterThanOrEqualTo(value, bound),
                propertyName + " must be greater than or equal to " + bound + ".");
    }

    public RuleBuilder<T, P> isInEnum() {
        return addRule(Rules::isInEnum, "Invalid " + propertyName + ".");
    }

    /** Overrides the message of the immediately preceding rule (FluentValidation {@code WithMessage}). */
    public RuleBuilder<T, P> withMessage(String message) {
        if (lastRule != null) {
            lastRule.overrideMessage(message);
        }
        return this;
    }
}
