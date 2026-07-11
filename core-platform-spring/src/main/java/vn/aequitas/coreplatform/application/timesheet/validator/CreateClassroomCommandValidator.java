package vn.aequitas.coreplatform.application.timesheet.validator;

import org.springframework.stereotype.Component;
import vn.aequitas.coreplatform.application.common.validation.AbstractValidator;
import vn.aequitas.coreplatform.application.timesheet.command.createclassroom.CreateClassroomCommand;

/** Port of the .NET {@code CreateClassroomCommandValidator}. */
@Component
public class CreateClassroomCommandValidator extends AbstractValidator<CreateClassroomCommand> {

    public CreateClassroomCommandValidator() {
        ruleFor("ClassCode", CreateClassroomCommand::getClassCode)
                .notEmpty().withMessage("Class code is required.")
                .maximumLength(50).withMessage("Class code cannot exceed 50 characters.");

        ruleFor("ClassName", CreateClassroomCommand::getClassName)
                .notEmpty().withMessage("Class name is required.")
                .maximumLength(100).withMessage("Class name cannot exceed 100 characters.");

        ruleFor("Location", CreateClassroomCommand::getLocation)
                .notEmpty().withMessage("Location is required.")
                .maximumLength(100).withMessage("Location cannot exceed 100 characters.");

        ruleFor("NumberOfStudent", CreateClassroomCommand::getNumberOfStudent)
                .greaterThan(0).withMessage("Number of students must be greater than 0.");

        ruleFor("Level", CreateClassroomCommand::getLevel)
                .isInEnum().withMessage("Invalid level.");

        ruleFor("Status", CreateClassroomCommand::getStatus)
                .isInEnum().withMessage("Invalid status.");
    }
}
