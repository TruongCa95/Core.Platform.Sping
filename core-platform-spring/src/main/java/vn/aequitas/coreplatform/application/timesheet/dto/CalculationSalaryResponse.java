package vn.aequitas.coreplatform.application.timesheet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Output of the salary calculation service. Port of the .NET
 * {@code CalculationSalaryResponse}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationSalaryResponse {

    private UUID classroomId;

    private BigDecimal salary;
}
