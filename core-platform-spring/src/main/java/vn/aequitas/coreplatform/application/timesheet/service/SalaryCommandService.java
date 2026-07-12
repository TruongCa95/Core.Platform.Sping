package vn.aequitas.coreplatform.application.timesheet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.aequitas.coreplatform.application.timesheet.command.createbasesalary.CreateBaseSalaryCommand;
import vn.aequitas.coreplatform.domain.entity.timesheet.Salary;
import vn.aequitas.coreplatform.domain.repository.SalaryRepository;

import java.util.UUID;

/**
 * Write-side operations for base-salary rows. Port of the former
 * {@code CreateBaseSalaryCommandHandler}; the command is validated up front by bean
 * validation ({@code @Valid} at the controller), so no in-service checks remain.
 */
@Service
public class SalaryCommandService {

    private final SalaryRepository salaries;

    public SalaryCommandService(SalaryRepository salaries) {
        this.salaries = salaries;
    }

    @Transactional
    public UUID createBaseSalary(CreateBaseSalaryCommand request) {
        Salary salary = new Salary();
        salary.setId(UUID.randomUUID());
        salary.setLevel(request.getLevel());
        salary.setMoney(request.getMoney());
        salary.setNumberOfStudent(request.getNumberOfStudent());

        salaries.save(salary);
        return salary.getId();
    }
}
