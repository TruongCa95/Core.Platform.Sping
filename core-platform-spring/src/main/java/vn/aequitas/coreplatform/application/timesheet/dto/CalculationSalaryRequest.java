package vn.aequitas.coreplatform.application.timesheet.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.domain.enums.KiEnums;
import vn.aequitas.coreplatform.domain.enums.LevelEnums;
import vn.aequitas.coreplatform.domain.enums.LocationEnums;

import java.util.UUID;

/**
 * Input to the salary calculation service. Port of the .NET
 * {@code CalculationSalaryRequest} (field names kept lower-camel to match its
 * JSON contract: {@code level}, {@code location}, {@code numberOfStudent}, {@code ki}).
 */
@Getter
@Setter
@NoArgsConstructor
public class CalculationSalaryRequest {

    private UUID classroomId;

    private LevelEnums level;

    private LocationEnums location;

    private int numberOfStudent;

    private KiEnums ki;
}
