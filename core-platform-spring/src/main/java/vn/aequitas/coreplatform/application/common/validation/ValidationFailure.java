package vn.aequitas.coreplatform.application.common.validation;

/**
 * A single validation error, the counterpart of FluentValidation's
 * {@code ValidationFailure}.
 *
 * @param propertyName the property that failed
 * @param errorMessage the human-readable message
 */
public record ValidationFailure(String propertyName, String errorMessage) {
}
