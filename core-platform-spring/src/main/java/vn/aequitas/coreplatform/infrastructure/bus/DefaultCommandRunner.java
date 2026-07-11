package vn.aequitas.coreplatform.infrastructure.bus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.aequitas.coreplatform.application.common.bus.CommandRunner;
import vn.aequitas.coreplatform.application.common.mediator.Mediator;
import vn.aequitas.coreplatform.application.common.mediator.Request;

/** Port of the .NET {@code Command} command runner (logs, then delegates to the mediator). */
@Component
public class DefaultCommandRunner implements CommandRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultCommandRunner.class);

    private final Mediator mediator;

    public DefaultCommandRunner(Mediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public <R> R send(Request<R> request) {
        log.info("Sending command: {}", request.getClass().getSimpleName());
        R response = mediator.send(request);
        log.info("Received response: {}", response);
        return response;
    }
}
