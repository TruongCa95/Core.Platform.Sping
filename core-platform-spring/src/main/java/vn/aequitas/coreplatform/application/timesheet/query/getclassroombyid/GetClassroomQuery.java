package vn.aequitas.coreplatform.application.timesheet.query.getclassroombyid;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.application.common.mediator.Request;

import java.util.UUID;

/** Port of the .NET {@code GetClassroomQuery}. */
@Getter
@Setter
@NoArgsConstructor
public class GetClassroomQuery implements Request<GetClassroomQueryResult> {

    private UUID classroomId;
}
