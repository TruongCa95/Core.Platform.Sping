package vn.aequitas.coreplatform.application.common.validation;

import org.springframework.stereotype.Component;
import vn.aequitas.coreplatform.application.common.mediator.PipelineBehavior;
import vn.aequitas.coreplatform.application.common.mediator.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Pipeline behaviour that runs every registered {@link Validator} for a request
 * before its handler executes, throwing {@link ValidationException} on failure.
 * Direct port of the .NET {@code ValidationBehavior<TRequest, TResponse>}.
 */
@Component
public class ValidationBehavior implements PipelineBehavior {

    private final ValidatorRegistry registry;

    public ValidationBehavior(ValidatorRegistry registry) {
        this.registry = registry;
    }

    @Override
    public <R> R handle(Request<R> request, Supplier<R> next) {
        List<Validator<Request<R>>> validators = registry.validatorsFor(request.getClass());
        if (!validators.isEmpty()) {
            List<ValidationFailure> failures = new ArrayList<>();
            for (Validator<Request<R>> validator : validators) {
                failures.addAll(validator.validate(request));
            }
            if (!failures.isEmpty()) {
                throw new ValidationException(failures);
            }
        }
        return next.get();
    }
}
