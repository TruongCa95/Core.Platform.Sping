package vn.aequitas.coreplatform.application.timesheet.query.getlisttimesheet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.application.timesheet.dto.TimeSheetDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** One month-group of timesheets with its totals. Port of the .NET {@code TimesheetResult}. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetResult {

    private String month = "";

    @Builder.Default
    private List<TimeSheetDTO> timeSheet = new ArrayList<>();

    private BigDecimal allowanceTotal;

    private BigDecimal grossTotal;

    private BigDecimal taxforCharity;

    private BigDecimal netTotal;
}
