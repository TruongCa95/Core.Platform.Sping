package vn.aequitas.coreplatform.application.timesheet.validator;

import org.springframework.stereotype.Component;
import vn.aequitas.coreplatform.application.common.validation.AbstractValidator;
import vn.aequitas.coreplatform.application.timesheet.command.createstudent.CreateStudentCommand;

/** Port of the .NET {@code CreateStudentCommandValidator}. */
@Component
public class CreateStudentCommandValidator extends AbstractValidator<CreateStudentCommand> {

    public CreateStudentCommandValidator() {
        ruleFor("Name", CreateStudentCommand::getName)
                .notEmpty().withMessage("Student name is required.")
                .maximumLength(100).withMessage("Student name cannot exceed 100 characters.");

        ruleFor("Grade", CreateStudentCommand::getGrade)
                .notEmpty().withMessage("Grade is required.")
                .maximumLength(50).withMessage("Grade cannot exceed 50 characters.");

        ruleFor("Review", CreateStudentCommand::getReview)
                .maximumLength(500).withMessage("Review cannot exceed 500 characters.");

        ruleFor("ClassroomIds", CreateStudentCommand::getClassroomIds)
                .notNull().withMessage("Classroom list cannot be null.");
    }
}
