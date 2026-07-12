package vn.aequitas.coreplatform.application.timesheet.command.updateclassroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.domain.enums.ClassRoomStatusEnums;
import vn.aequitas.coreplatform.domain.enums.LevelEnums;

import java.util.UUID;

/**
 * Port of the .NET {@code UpdateClassroomCommand}. Returns whether the row was updated.
 * The {@code id} is taken from the request path by the controller, so it carries no
 * bean-validation constraint here.
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateClassroomCommand {

    private UUID id;

    @NotBlank(message = "Class code is required.")
    @Size(max = 50, message = "Class code cannot exceed 50 characters.")
    private String classCode;

    @NotBlank(message = "Class name is required.")
    @Size(max = 100, message = "Class name cannot exceed 100 characters.")
    private String className;

    @NotBlank(message = "Location is required.")
    @Size(max = 100, message = "Location cannot exceed 100 characters.")
    private String location;

    @Positive(message = "Number of students must be greater than 0.")
    private int numberOfStudent;

    @NotNull(message = "Invalid level.")
    private LevelEnums level;

    @NotNull(message = "Invalid status.")
    private ClassRoomStatusEnums status = ClassRoomStatusEnums.Active;
}
