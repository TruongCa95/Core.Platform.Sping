package vn.aequitas.coreplatform.application.timesheet.validator;

import org.springframework.stereotype.Component;
import vn.aequitas.coreplatform.application.common.validation.AbstractValidator;
import vn.aequitas.coreplatform.application.timesheet.command.createbasesalary.CreateBaseSalaryCommand;

import java.math.BigDecimal;

/** Port of the .NET {@code CreateBaseSalaryCommandValidator}. */
@Component
public class CreateBaseSalaryCommandValidator extends AbstractValidator<CreateBaseSalaryCommand> {

    public CreateBaseSalaryCommandValidator() {
        ruleFor("Level", CreateBaseSalaryCommand::getLevel)
                .isInEnum().withMessage("Invalid level.");

        ruleFor("Money", CreateBaseSalaryCommand::getMoney)
                .greaterThan(BigDecimal.ZERO).withMessage("Money must be greater than 0.");

        ruleFor("NumberOfStudent", CreateBaseSalaryCommand::getNumberOfStudent)
                .greaterThanOrEqualTo(0).withMessage("Number of students cannot be negative.");
    }
}
