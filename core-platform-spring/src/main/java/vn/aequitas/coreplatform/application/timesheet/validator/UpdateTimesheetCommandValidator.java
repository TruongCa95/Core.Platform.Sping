package vn.aequitas.coreplatform.application.timesheet.validator;

import org.springframework.stereotype.Component;
import vn.aequitas.coreplatform.application.common.validation.AbstractValidator;
import vn.aequitas.coreplatform.application.timesheet.command.updatetimesheet.UpdateTimesheetCommand;

/** Port of the .NET {@code UpdateTimesheetCommandValidator}. */
@Component
public class UpdateTimesheetCommandValidator extends AbstractValidator<UpdateTimesheetCommand> {

    public UpdateTimesheetCommandValidator() {
        ruleFor("Id", UpdateTimesheetCommand::getId)
                .notEmpty().withMessage("Timesheet id is required.");

        ruleFor("Description", UpdateTimesheetCommand::getDescription)
                .notEmpty().withMessage("Description is required.")
                .maximumLength(500).withMessage("Description cannot exceed 500 characters.");

        ruleFor("Date", UpdateTimesheetCommand::getDate)
                .notEmpty().withMessage("Date is required.");
    }
}
