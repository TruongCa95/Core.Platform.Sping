package vn.aequitas.coreplatform.application.timesheet.command.createstudent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Port of the .NET {@code CreateStudentCommand}. Returns the new student id. */
@Getter
@Setter
@NoArgsConstructor
public class CreateStudentCommand {

    @NotBlank(message = "Student name is required.")
    @Size(max = 100, message = "Student name cannot exceed 100 characters.")
    private String name = "";

    @NotBlank(message = "Grade is required.")
    @Size(max = 50, message = "Grade cannot exceed 50 characters.")
    private String grade = "";

    @Size(max = 500, message = "Review cannot exceed 500 characters.")
    private String review = "";

    @NotNull(message = "Classroom list cannot be null.")
    private List<UUID> classroomIds = new ArrayList<>();
}
