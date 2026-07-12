package vn.aequitas.coreplatform.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.aequitas.coreplatform.domain.entity.timesheet.Salary;

import java.util.UUID;

/** Spring Data repository for {@link Salary} (base-salary rows). */
public interface SalaryRepository
        extends JpaRepository<Salary, UUID>, JpaSpecificationExecutor<Salary> {
}
