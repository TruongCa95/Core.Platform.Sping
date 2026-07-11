package vn.aequitas.coreplatform.application.common.mediator;

/**
 * Handles exactly one {@link Request} type. Counterpart of MediatR's
 * {@code IRequestHandler<TRequest, TResponse>}. Each implementation is a Spring
 * bean; the {@link Mediator} indexes them by their concrete request type.
 *
 * @param <C> request type handled
 * @param <R> response type produced
 */
public interface RequestHandler<C extends Request<R>, R> {

    R handle(C request);
}
