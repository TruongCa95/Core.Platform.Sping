package vn.aequitas.coreplatform.application.timesheet.query.getlisttimesheet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Paginated month-grouped timesheet response. Port of the .NET {@code PagedTimesheetResult}. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedTimesheetResult {

    @Builder.Default
    private List<TimesheetResult> results = new ArrayList<>();

    private int page;

    private int pageSize;

    private int totalCount;

    private int totalPages;
}
