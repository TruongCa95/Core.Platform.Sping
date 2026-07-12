package vn.aequitas.coreplatform.application.timesheet.command.createtimesheet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.application.timesheet.dto.ClassroomDTO;
import vn.aequitas.coreplatform.application.timesheet.dto.TimesheetReviewDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Port of the .NET {@code CreateTimesheetCommand}. Returns the new timesheet id. */
@Getter
@Setter
@NoArgsConstructor
public class CreateTimesheetCommand {

    @NotBlank(message = "Description is required.")
    @Size(max = 500, message = "Description cannot exceed 500 characters.")
    private String description = "";

    @NotNull(message = "Date is required.")
    private LocalDateTime date;

    private List<ClassroomDTO> classrooms = new ArrayList<>();

    private List<TimesheetReviewDTO> timesheetReviews = new ArrayList<>();
}
