package vn.aequitas.coreplatform.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.aequitas.coreplatform.domain.entity.timesheet.StudentClasses;

import java.util.UUID;

/** Spring Data repository for the {@link StudentClasses} enrolment rows. */
public interface StudentClassesRepository
        extends JpaRepository<StudentClasses, UUID>, JpaSpecificationExecutor<StudentClasses> {
}
