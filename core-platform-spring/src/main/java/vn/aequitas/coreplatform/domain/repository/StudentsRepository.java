package vn.aequitas.coreplatform.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.aequitas.coreplatform.domain.entity.timesheet.Students;

import java.util.UUID;

/** Spring Data repository for {@link Students}. */
public interface StudentsRepository
        extends JpaRepository<Students, UUID>, JpaSpecificationExecutor<Students> {
}
