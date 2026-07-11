package vn.aequitas.coreplatform.application.timesheet.command.deletestudent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.application.common.mediator.Request;

import java.util.UUID;

/** Port of the .NET {@code DeleteStudentByIdCommand}. */
@Getter
@Setter
@NoArgsConstructor
public class DeleteStudentByIdCommand implements Request<Boolean> {

    private UUID id;
}
