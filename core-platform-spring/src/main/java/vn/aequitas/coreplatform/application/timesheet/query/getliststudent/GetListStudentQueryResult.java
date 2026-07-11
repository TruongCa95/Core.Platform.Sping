package vn.aequitas.coreplatform.application.timesheet.query.getliststudent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Port of the .NET {@code GetListStudentQueryResult}. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetListStudentQueryResult {

    private UUID id;

    private String name;

    private String grade;

    private String review;

    @Builder.Default
    private List<UUID> classroomIds = new ArrayList<>();
}
