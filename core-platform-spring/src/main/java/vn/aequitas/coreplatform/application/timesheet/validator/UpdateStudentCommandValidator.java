package vn.aequitas.coreplatform.application.timesheet.validator;

import org.springframework.stereotype.Component;
import vn.aequitas.coreplatform.application.common.validation.AbstractValidator;
import vn.aequitas.coreplatform.application.timesheet.command.updatestudent.UpdateStudentCommand;

/** Port of the .NET {@code UpdateStudentCommandValidator}. */
@Component
public class UpdateStudentCommandValidator extends AbstractValidator<UpdateStudentCommand> {

    public UpdateStudentCommandValidator() {
        ruleFor("Id", UpdateStudentCommand::getId)
                .notEmpty().withMessage("Student id is required.");

        ruleFor("Name", UpdateStudentCommand::getName)
                .notEmpty().withMessage("Student name is required.")
                .maximumLength(100).withMessage("Student name cannot exceed 100 characters.");

        ruleFor("Grade", UpdateStudentCommand::getGrade)
                .notEmpty().withMessage("Grade is required.")
                .maximumLength(50).withMessage("Grade cannot exceed 50 characters.");

        ruleFor("Review", UpdateStudentCommand::getReview)
                .maximumLength(500).withMessage("Review cannot exceed 500 characters.");
    }
}
