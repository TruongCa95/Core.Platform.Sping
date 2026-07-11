package vn.aequitas.coreplatform.application.timesheet.validator;

import org.springframework.stereotype.Component;
import vn.aequitas.coreplatform.application.common.validation.AbstractValidator;
import vn.aequitas.coreplatform.application.timesheet.command.createtimesheet.CreateTimesheetCommand;

/** Port of the .NET {@code CreateTimesheetCommandValidator}. */
@Component
public class CreateTimesheetCommandValidator extends AbstractValidator<CreateTimesheetCommand> {

    public CreateTimesheetCommandValidator() {
        ruleFor("Description", CreateTimesheetCommand::getDescription)
                .notEmpty().withMessage("Description is required.")
                .maximumLength(500).withMessage("Description cannot exceed 500 characters.");

        ruleFor("Date", CreateTimesheetCommand::getDate)
                .notEmpty().withMessage("Date is required.");
    }
}
