package vn.aequitas.coreplatform.application.common.exception;

/**
 * Raised when a required record does not exist. Counterpart of the .NET
 * {@code KeyNotFoundException}; surfaced as HTTP 404.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super("Record not found.");
    }

    public NotFoundException(String message) {
        super(message);
    }
}
