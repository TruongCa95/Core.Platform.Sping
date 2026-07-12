package vn.aequitas.coreplatform.application.timesheet.command.createclassroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.domain.enums.ClassRoomStatusEnums;
import vn.aequitas.coreplatform.domain.enums.LevelEnums;

/** Port of the .NET {@code CreateClassroomCommand}. Returns the new classroom id. */
@Getter
@Setter
@NoArgsConstructor
public class CreateClassroomCommand {

    @NotBlank(message = "Class code is required.")
    @Size(max = 50, message = "Class code cannot exceed 50 characters.")
    private String classCode = "";

    @NotBlank(message = "Location is required.")
    @Size(max = 100, message = "Location cannot exceed 100 characters.")
    private String location = "";

    @NotBlank(message = "Class name is required.")
    @Size(max = 100, message = "Class name cannot exceed 100 characters.")
    private String className = "";

    @NotNull(message = "Invalid level.")
    private LevelEnums level;

    @Positive(message = "Number of students must be greater than 0.")
    private int numberOfStudent = 1;

    @NotNull(message = "Invalid status.")
    private ClassRoomStatusEnums status = ClassRoomStatusEnums.Active;
}
