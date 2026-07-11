package vn.aequitas.coreplatform.application.timesheet.query.getlistclassroom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Port of the .NET {@code GetListClassroomQueryResult}. {@code level}/{@code status} are numeric enum values. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetListClassroomQueryResult {

    private UUID id;

    private String classCode;

    private String className;

    private String location;

    private int numberOfStudent;

    private int level;

    private int status;
}
