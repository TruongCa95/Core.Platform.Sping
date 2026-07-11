package vn.aequitas.coreplatform.application.common.validation;

import org.springframework.core.GenericTypeResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Base class for validators, the counterpart of FluentValidation's
 * {@code AbstractValidator<T>}. Subclasses declare rules in their constructor via
 * {@link #ruleFor}. The target type is resolved reflectively so validators can be
 * indexed by request type.
 *
 * @param <T> the validated type
 */
public abstract class AbstractValidator<T> implements Validator<T> {

    private final List<Rule<T>> rules = new ArrayList<>();
    private final Class<T> targetType;

    @SuppressWarnings("unchecked")
    protected AbstractValidator() {
        this.targetType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), AbstractValidator.class);
    }

    /**
     * Begins a rule chain for a property.
     *
     * @param propertyName name reported in failures (matches the C# member name)
     * @param selector     reads the property value from the instance
     */
    protected <P> RuleBuilder<T, P> ruleFor(String propertyName, Function<T, P> selector) {
        return new RuleBuilder<>(this, propertyName, selector);
    }

    void register(Rule<T> rule) {
        rules.add(rule);
    }

    @Override
    public List<ValidationFailure> validate(T instance) {
        List<ValidationFailure> failures = new ArrayList<>();
        for (Rule<T> rule : rules) {
            rule.evaluate(instance, failures);
        }
        return failures;
    }

    @Override
    public Class<T> getTargetType() {
        return targetType;
    }
}
