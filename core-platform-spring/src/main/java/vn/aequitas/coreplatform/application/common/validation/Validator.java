package vn.aequitas.coreplatform.application.common.validation;

import java.util.List;

/**
 * Validates a single request/command type. Counterpart of FluentValidation's
 * {@code IValidator<T>}. Most implementations extend {@link AbstractValidator}.
 *
 * @param <T> the type validated
 */
public interface Validator<T> {

    /** Returns the failures for {@code instance}; empty when valid. */
    List<ValidationFailure> validate(T instance);

    /** The concrete type this validator targets (used to index validators by request type). */
    Class<T> getTargetType();
}
