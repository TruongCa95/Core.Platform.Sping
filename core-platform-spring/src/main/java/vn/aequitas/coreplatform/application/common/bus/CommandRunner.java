package vn.aequitas.coreplatform.application.common.bus;

import vn.aequitas.coreplatform.application.common.mediator.Request;

/**
 * Thin command dispatch facade, the counterpart of the .NET {@code ICommandRunner}.
 * Kept as a distinct type from {@link QueryRunner} so controllers express intent
 * (write vs read) even though both delegate to the mediator.
 */
public interface CommandRunner {

    <R> R send(Request<R> request);
}
