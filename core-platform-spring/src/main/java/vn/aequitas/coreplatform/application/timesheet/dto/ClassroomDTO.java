package vn.aequitas.coreplatform.application.timesheet.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Classroom reference carried inside timesheet create/update payloads.
 * Port of the .NET {@code ClassroomDTO}.
 */
@Getter
@Setter
@NoArgsConstructor
public class ClassroomDTO {

    private int numberOfStudent;

    private UUID classroomId;
}
