package vn.aequitas.coreplatform.application.timesheet.command.deleteclassroom;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.application.common.mediator.Request;

import java.util.UUID;

/** Port of the .NET {@code DeleteClassroomByIdCommand}. */
@Getter
@Setter
@NoArgsConstructor
public class DeleteClassroomByIdCommand implements Request<Boolean> {

    private UUID id;
}
