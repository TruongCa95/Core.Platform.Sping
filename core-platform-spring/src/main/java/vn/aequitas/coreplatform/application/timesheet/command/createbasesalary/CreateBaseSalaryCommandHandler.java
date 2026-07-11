package vn.aequitas.coreplatform.application.timesheet.command.createbasesalary;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.aequitas.coreplatform.application.common.mediator.RequestHandler;
import vn.aequitas.coreplatform.application.common.validation.ValidationException;
import vn.aequitas.coreplatform.application.common.validation.ValidationFailure;
import vn.aequitas.coreplatform.application.timesheet.validator.CreateBaseSalaryCommandValidator;
import vn.aequitas.coreplatform.domain.entity.timesheet.Salary;
import vn.aequitas.coreplatform.domain.repository.UnitOfWork;

import java.util.List;
import java.util.UUID;

/**
 * Port of the .NET {@code CreateBaseSalaryCommandHandler}. Keeps the original's
 * explicit in-handler validation (in addition to the validation pipeline).
 */
@Component
public class CreateBaseSalaryCommandHandler implements RequestHandler<CreateBaseSalaryCommand, UUID> {

    private final UnitOfWork unitOfWork;
    private final CreateBaseSalaryCommandValidator validator;

    public CreateBaseSalaryCommandHandler(UnitOfWork unitOfWork, CreateBaseSalaryCommandValidator validator) {
        this.unitOfWork = unitOfWork;
        this.validator = validator;
    }

    @Override
    @Transactional
    public UUID handle(CreateBaseSalaryCommand request) {
        List<ValidationFailure> failures = validator.validate(request);
        if (!failures.isEmpty()) {
            throw new ValidationException(failures);
        }

        Salary salary = new Salary();
        salary.setId(UUID.randomUUID());
        salary.setLevel(request.getLevel());
        salary.setMoney(request.getMoney());
        salary.setNumberOfStudent(request.getNumberOfStudent());

        unitOfWork.salaries().add(salary);
        unitOfWork.complete();
        return salary.getId();
    }
}
