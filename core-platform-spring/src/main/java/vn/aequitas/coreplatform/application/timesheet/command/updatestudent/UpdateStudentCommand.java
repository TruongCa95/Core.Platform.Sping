package vn.aequitas.coreplatform.application.timesheet.command.updatestudent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Port of the .NET {@code UpdateStudentCommand}. The {@code id} is taken from the
 * request path by the controller, so it carries no bean-validation constraint here.
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateStudentCommand {

    private UUID id;

    @NotBlank(message = "Student name is required.")
    @Size(max = 100, message = "Student name cannot exceed 100 characters.")
    private String name;

    @NotBlank(message = "Grade is required.")
    @Size(max = 50, message = "Grade cannot exceed 50 characters.")
    private String grade;

    @Size(max = 500, message = "Review cannot exceed 500 characters.")
    private String review;

    /**
     * When provided (even empty), the student's enrolments are reconciled to
     * exactly match this list. When {@code null}, enrolments are left untouched.
     */
    private List<UUID> classroomIds;
}
