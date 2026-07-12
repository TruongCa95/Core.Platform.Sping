package vn.aequitas.coreplatform.application.timesheet.command.updatetimesheet;

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
import java.util.UUID;

/**
 * Port of the .NET {@code UpdateTimesheetCommand}. The {@code id} is taken from the
 * request path by the controller, so it carries no bean-validation constraint here.
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateTimesheetCommand {

    private UUID id;

    @NotBlank(message = "Description is required.")
    @Size(max = 500, message = "Description cannot exceed 500 characters.")
    private String description = "";

    @NotNull(message = "Date is required.")
    private LocalDateTime date;

    private List<ClassroomDTO> classrooms = new ArrayList<>();

    private List<TimesheetReviewDTO> timesheetReviews = new ArrayList<>();
}
