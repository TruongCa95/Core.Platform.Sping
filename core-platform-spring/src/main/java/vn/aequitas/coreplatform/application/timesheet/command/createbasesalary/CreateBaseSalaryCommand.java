package vn.aequitas.coreplatform.application.timesheet.command.createbasesalary;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.aequitas.coreplatform.application.common.mediator.Request;
import vn.aequitas.coreplatform.domain.enums.LevelEnums;

import java.math.BigDecimal;
import java.util.UUID;

/** Port of the .NET {@code CreateBaseSalaryCommand}. Returns the new salary id. */
@Getter
@Setter
@NoArgsConstructor
public class CreateBaseSalaryCommand implements Request<UUID> {

    private LevelEnums level;

    private BigDecimal money;

    private int numberOfStudent;
}
