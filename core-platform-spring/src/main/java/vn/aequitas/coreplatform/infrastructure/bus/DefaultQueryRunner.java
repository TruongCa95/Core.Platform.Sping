package vn.aequitas.coreplatform.infrastructure.bus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.aequitas.coreplatform.application.common.bus.QueryRunner;
import vn.aequitas.coreplatform.application.common.mediator.Mediator;
import vn.aequitas.coreplatform.application.common.mediator.Request;

/** Port of the .NET {@code QueryRunner} (logs, then delegates to the mediator). */
@Component
public class DefaultQueryRunner implements QueryRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultQueryRunner.class);

    private final Mediator mediator;

    public DefaultQueryRunner(Mediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public <R> R send(Request<R> request) {
        log.info("Sending query: {}", request.getClass().getSimpleName());
        R response = mediator.send(request);
        log.info("Received response: {}", response);
        return response;
    }
}
