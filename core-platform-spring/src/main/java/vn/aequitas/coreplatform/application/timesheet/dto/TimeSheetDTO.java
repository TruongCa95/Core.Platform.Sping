package vn.aequitas.coreplatform.application.timesheet.dto;

import lombok.Getter;
import lombok.Setter;
import vn.aequitas.coreplatform.domain.enums.LevelEnums;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Flattened timesheet row returned by the list query, including the computed
 * salary/allowance figures. Port of the .NET {@code TimeSheetDTO}.
 */
@Getter
@Setter
public class TimeSheetDTO {

    private UUID id;

    private UUID classroomId;

    private String description = "";

    private String classcode = "";

    private LocalDateTime date;

    private int numberOfStudent;

    private LevelEnums level;

    private BigDecimal allowance = BigDecimal.ZERO;

    private BigDecimal salary = BigDecimal.ZERO;

    private BigDecimal totalSalary = BigDecimal.ZERO;

    private List<TimesheetReviewDTO> reviews = new ArrayList<>();
}
