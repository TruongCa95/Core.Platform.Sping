package vn.aequitas.coreplatform.application.timesheet.validator;

import org.springframework.stereotype.Component;
import vn.aequitas.coreplatform.application.common.validation.AbstractValidator;
import vn.aequitas.coreplatform.application.timesheet.command.updateclassroom.UpdateClassroomCommand;

/** Port of the .NET {@code UpdateClassroomCommandValidator}. */
@Component
public class UpdateClassroomCommandValidator extends AbstractValidator<UpdateClassroomCommand> {

    public UpdateClassroomCommandValidator() {
        ruleFor("Id", UpdateClassroomCommand::getId)
                .notEmpty().withMessage("Classroom id is required.");

        ruleFor("ClassCode", UpdateClassroomCommand::getClassCode)
                .notEmpty().withMessage("Class code is required.")
                .maximumLength(50).withMessage("Class code cannot exceed 50 characters.");

        ruleFor("ClassName", UpdateClassroomCommand::getClassName)
                .notEmpty().withMessage("Class name is required.")
                .maximumLength(100).withMessage("Class name cannot exceed 100 characters.");

        ruleFor("Location", UpdateClassroomCommand::getLocation)
                .notEmpty().withMessage("Location is required.")
                .maximumLength(100).withMessage("Location cannot exceed 100 characters.");

        ruleFor("NumberOfStudent", UpdateClassroomCommand::getNumberOfStudent)
                .greaterThan(0).withMessage("Number of students must be greater than 0.");

        ruleFor("Level", UpdateClassroomCommand::getLevel)
                .isInEnum().withMessage("Invalid level.");

        ruleFor("Status", UpdateClassroomCommand::getStatus)
                .isInEnum().withMessage("Invalid status.");
    }
}
