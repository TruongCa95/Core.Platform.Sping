package vn.aequitas.coreplatform.application.timesheet.command.createbasesalary;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.domain.enums.LevelEnums;

import java.math.BigDecimal;

/** Port of the .NET {@code CreateBaseSalaryCommand}. Returns the new salary id. */
@Getter
@Setter
@NoArgsConstructor
public class CreateBaseSalaryCommand {

    @NotNull(message = "Invalid level.")
    private LevelEnums level;

    @NotNull(message = "Money must be greater than 0.")
    @DecimalMin(value = "0", inclusive = false, message = "Money must be greater than 0.")
    private BigDecimal money;

    @PositiveOrZero(message = "Number of students cannot be negative.")
    private int numberOfStudent;
}
