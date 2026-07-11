package vn.aequitas.coreplatform.application.common.validation;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Indexes every {@link Validator} bean by its target request type so the
 * {@link ValidationBehavior} can look up the validators for a given request -
 * the Spring equivalent of MediatR resolving {@code IEnumerable<IValidator<T>>}.
 */
@Component
public class ValidatorRegistry {

    private final Map<Class<?>, List<Validator<?>>> byType = new HashMap<>();

    public ValidatorRegistry(List<Validator<?>> validators) {
        for (Validator<?> validator : validators) {
            byType.computeIfAbsent(validator.getTargetType(), key -> new ArrayList<>()).add(validator);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<Validator<T>> validatorsFor(Class<?> requestType) {
        List<Validator<?>> found = byType.get(requestType);
        if (found == null) {
            return List.of();
        }
        List<Validator<T>> typed = new ArrayList<>(found.size());
        for (Validator<?> validator : found) {
            typed.add((Validator<T>) validator);
        }
        return typed;
    }
}
