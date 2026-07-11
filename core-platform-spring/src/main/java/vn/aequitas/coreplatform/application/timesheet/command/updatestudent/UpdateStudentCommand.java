package vn.aequitas.coreplatform.application.timesheet.command.updatestudent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.application.common.mediator.Request;

import java.util.List;
import java.util.UUID;

/** Port of the .NET {@code UpdateStudentCommand}. */
@Getter
@Setter
@NoArgsConstructor
public class UpdateStudentCommand implements Request<Boolean> {

    private UUID id;

    private String name;

    private String grade;

    private String review;

    /**
     * When provided (even empty), the student's enrolments are reconciled to
     * exactly match this list. When {@code null}, enrolments are left untouched.
     */
    private List<UUID> classroomIds;
}
