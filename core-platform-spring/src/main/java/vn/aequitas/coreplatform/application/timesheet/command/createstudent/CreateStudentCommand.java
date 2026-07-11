package vn.aequitas.coreplatform.application.timesheet.command.createstudent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.application.common.mediator.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Port of the .NET {@code CreateStudentCommand}. Returns the new student id. */
@Getter
@Setter
@NoArgsConstructor
public class CreateStudentCommand implements Request<UUID> {

    private String name = "";

    private String grade = "";

    private String review = "";

    private List<UUID> classroomIds = new ArrayList<>();
}
