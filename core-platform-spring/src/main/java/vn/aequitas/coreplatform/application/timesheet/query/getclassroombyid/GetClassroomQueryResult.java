package vn.aequitas.coreplatform.application.timesheet.query.getclassroombyid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Port of the .NET {@code GetClassroomQueryResult}. {@code status} is the numeric enum value. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetClassroomQueryResult {

    private UUID id;

    private String className;

    private String classCode;

    private int numberOfStudent;

    private int status;
}
