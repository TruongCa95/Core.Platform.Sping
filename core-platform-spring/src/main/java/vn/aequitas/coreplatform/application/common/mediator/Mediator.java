package vn.aequitas.coreplatform.application.common.mediator;

/**
 * Dispatches a {@link Request} to its single registered {@link RequestHandler},
 * running it through the configured {@link PipelineBehavior}s. Counterpart of
 * MediatR's {@code IMediator}.
 */
public interface Mediator {

    <R> R send(Request<R> request);
}
