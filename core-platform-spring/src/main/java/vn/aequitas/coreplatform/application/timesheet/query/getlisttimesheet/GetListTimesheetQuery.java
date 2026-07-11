package vn.aequitas.coreplatform.application.timesheet.query.getlisttimesheet;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.application.common.mediator.Request;

/** Port of the .NET {@code GetListTimesheetQuery}. */
@Getter
@Setter
@NoArgsConstructor
public class GetListTimesheetQuery implements Request<PagedTimesheetResult> {

    private String month;

    private Integer year;

    private int page = 1;

    private int pageSize = 20;
}
