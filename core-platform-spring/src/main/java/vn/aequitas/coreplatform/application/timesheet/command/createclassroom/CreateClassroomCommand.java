package vn.aequitas.coreplatform.application.timesheet.command.createclassroom;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.application.common.mediator.Request;
import vn.aequitas.coreplatform.domain.enums.ClassRoomStatusEnums;
import vn.aequitas.coreplatform.domain.enums.LevelEnums;

import java.util.UUID;

/** Port of the .NET {@code CreateClassroomCommand}. Returns the new classroom id. */
@Getter
@Setter
@NoArgsConstructor
public class CreateClassroomCommand implements Request<UUID> {

    private String classCode = "";

    private String location = "";

    private String className = "";

    private LevelEnums level;

    private int numberOfStudent = 1;

    private ClassRoomStatusEnums status = ClassRoomStatusEnums.Active;
}
