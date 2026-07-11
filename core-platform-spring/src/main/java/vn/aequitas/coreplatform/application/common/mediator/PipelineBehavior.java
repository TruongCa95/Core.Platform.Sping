package vn.aequitas.coreplatform.application.common.mediator;

import java.util.function.Supplier;

/**
 * Cross-cutting step wrapped around every request/handler invocation, the
 * counterpart of MediatR's {@code IPipelineBehavior<TRequest, TResponse>}.
 * Implementations call {@code next.get()} to continue the pipeline (or short-circuit).
 *
 * <p>Declared with a generic method rather than a parameterized type so a single
 * behaviour instance (e.g. validation) can apply to every request without
 * generic-variance gymnastics.</p>
 */
public interface PipelineBehavior {

    <R> R handle(Request<R> request, Supplier<R> next);
}
