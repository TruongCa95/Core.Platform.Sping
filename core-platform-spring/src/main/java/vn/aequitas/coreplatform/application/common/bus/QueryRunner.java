package vn.aequitas.coreplatform.application.common.bus;

import vn.aequitas.coreplatform.application.common.mediator.Request;

/**
 * Thin query dispatch facade, the counterpart of the .NET {@code IQueryRunner}.
 */
public interface QueryRunner {

    <R> R send(Request<R> request);
}
