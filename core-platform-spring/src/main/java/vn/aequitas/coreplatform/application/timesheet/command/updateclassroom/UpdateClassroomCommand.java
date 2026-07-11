package vn.aequitas.coreplatform.application.timesheet.command.updateclassroom;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.application.common.mediator.Request;
import vn.aequitas.coreplatform.domain.enums.ClassRoomStatusEnums;
import vn.aequitas.coreplatform.domain.enums.LevelEnums;

import java.util.UUID;

/** Port of the .NET {@code UpdateClassroomCommand}. Returns whether the row was updated. */
@Getter
@Setter
@NoArgsConstructor
public class UpdateClassroomCommand implements Request<Boolean> {

    private UUID id;

    private String classCode;

    private String className;

    private String location;

    private int numberOfStudent;

    private LevelEnums level;

    private ClassRoomStatusEnums status = ClassRoomStatusEnums.Active;
}
