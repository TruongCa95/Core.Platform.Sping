package vn.aequitas.coreplatform.web.advice;

/**
 * JSON error body {@code { "error": ..., "statusCode": ... }}, matching the shape
 * produced by the .NET {@code ErrorHandlingMiddleware}.
 */
public record ErrorResponse(String error, int statusCode) {
}
