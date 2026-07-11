package vn.aequitas.coreplatform.application.timesheet.query.getlistclassroom;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.application.common.dto.PagedResult;
import vn.aequitas.coreplatform.application.common.mediator.Request;

/** Port of the .NET {@code GetListClassroomQuery}. */
@Getter
@Setter
@NoArgsConstructor
public class GetListClassroomQuery implements Request<PagedResult<GetListClassroomQueryResult>> {

    private int page = 1;

    private int pageSize = 20;

    private String search;
}
