package vn.aequitas.coreplatform.infrastructure.mediator;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import vn.aequitas.coreplatform.application.common.mediator.Mediator;
import vn.aequitas.coreplatform.application.common.mediator.PipelineBehavior;
import vn.aequitas.coreplatform.application.common.mediator.Request;
import vn.aequitas.coreplatform.application.common.mediator.RequestHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Default {@link Mediator}. On startup it indexes every {@link RequestHandler}
 * bean by the concrete request type it handles (resolved from its generic
 * signature), then dispatches each request through the ordered
 * {@link PipelineBehavior} chain to that handler. Counterpart of MediatR's
 * {@code Mediator}.
 */
@Component
public class DefaultMediator implements Mediator {

    private final Map<Class<?>, RequestHandler<?, ?>> handlers = new HashMap<>();
    private final List<PipelineBehavior> behaviors;

    public DefaultMediator(List<RequestHandler<?, ?>> handlerBeans, List<PipelineBehavior> behaviors) {
        this.behaviors = behaviors;
        for (RequestHandler<?, ?> handler : handlerBeans) {
            Class<?> requestType = resolveRequestType(handler);
            if (requestType == null) {
                throw new IllegalStateException(
                        "Could not resolve request type for handler " + handler.getClass().getName());
            }
            RequestHandler<?, ?> existing = handlers.putIfAbsent(requestType, handler);
            if (existing != null) {
                throw new IllegalStateException(
                        "Multiple handlers registered for request " + requestType.getName());
            }
        }
    }

    private Class<?> resolveRequestType(RequestHandler<?, ?> handler) {
        Class<?> targetClass = AopUtils.getTargetClass(handler);
        return ResolvableType.forClass(targetClass)
                .as(RequestHandler.class)
                .getGeneric(0)
                .resolve();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R send(Request<R> request) {
        RequestHandler<Request<R>, R> handler =
                (RequestHandler<Request<R>, R>) handlers.get(request.getClass());
        if (handler == null) {
            throw new IllegalStateException("No handler registered for request " + request.getClass().getName());
        }

        Supplier<R> chain = () -> handler.handle(request);
        for (int i = behaviors.size() - 1; i >= 0; i--) {
            PipelineBehavior behavior = behaviors.get(i);
            Supplier<R> next = chain;
            chain = () -> behavior.handle(request, next);
        }
        return chain.get();
    }
}
