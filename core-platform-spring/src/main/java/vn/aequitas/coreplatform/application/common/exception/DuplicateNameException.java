package vn.aequitas.coreplatform.application.common.exception;

/**
 * Raised when a create/update would collide with an existing active record
 * (e.g. duplicate class code or student name). Counterpart of the .NET
 * {@code System.Data.DuplicateNameException}; surfaced as HTTP 409.
 */
public class DuplicateNameException extends RuntimeException {

    public DuplicateNameException() {
        super("Duplicate data error.");
    }

    public DuplicateNameException(String message) {
        super(message);
    }
}
