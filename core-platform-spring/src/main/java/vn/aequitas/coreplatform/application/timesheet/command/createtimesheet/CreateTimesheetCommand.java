package vn.aequitas.coreplatform.application.timesheet.command.createtimesheet;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.application.common.mediator.Request;
import vn.aequitas.coreplatform.application.timesheet.dto.ClassroomDTO;
import vn.aequitas.coreplatform.application.timesheet.dto.TimesheetReviewDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Port of the .NET {@code CreateTimesheetCommand}. Returns the new timesheet id. */
@Getter
@Setter
@NoArgsConstructor
public class CreateTimesheetCommand implements Request<UUID> {

    private String description = "";

    private LocalDateTime date;

    private List<ClassroomDTO> classrooms = new ArrayList<>();

    private List<TimesheetReviewDTO> timesheetReviews = new ArrayList<>();
}
