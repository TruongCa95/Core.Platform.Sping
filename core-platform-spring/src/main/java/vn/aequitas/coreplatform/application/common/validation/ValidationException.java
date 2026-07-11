package vn.aequitas.coreplatform.application.common.validation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown when one or more rules fail. Counterpart of FluentValidation's
 * {@code ValidationException}; the message concatenates the individual failures
 * the same way, and is surfaced as HTTP 400 by the global exception handler.
 */
public class ValidationException extends RuntimeException {

    private final transient List<ValidationFailure> errors;

    public ValidationException(List<ValidationFailure> errors) {
        super(buildMessage(errors));
        this.errors = errors;
    }

    public List<ValidationFailure> getErrors() {
        return errors;
    }

    private static String buildMessage(List<ValidationFailure> errors) {
        String details = errors.stream()
                .map(f -> " -- " + f.propertyName() + ": " + f.errorMessage())
                .collect(Collectors.joining(System.lineSeparator()));
        return "Validation failed: " + System.lineSeparator() + details;
    }
}
