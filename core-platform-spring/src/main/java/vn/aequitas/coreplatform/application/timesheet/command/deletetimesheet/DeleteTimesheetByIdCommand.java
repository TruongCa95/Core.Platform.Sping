package vn.aequitas.coreplatform.application.timesheet.command.deletetimesheet;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.application.common.mediator.Request;

import java.util.UUID;

/** Port of the .NET {@code DeleteTimesheetByIdCommand}. */
@Getter
@Setter
@NoArgsConstructor
public class DeleteTimesheetByIdCommand implements Request<Boolean> {

    private UUID id;
}
