package vn.aequitas.coreplatform.application.common.mediator;

/**
 * Marker for a command or query that can be dispatched through the {@link Mediator}.
 * The counterpart of MediatR's {@code IRequest<TResponse>}.
 *
 * @param <R> response type produced by the matching {@link RequestHandler}
 */
public interface Request<R> {
}
