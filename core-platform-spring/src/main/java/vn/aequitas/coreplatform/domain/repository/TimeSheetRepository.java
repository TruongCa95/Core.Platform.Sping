package vn.aequitas.coreplatform.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimeSheet;

import java.util.UUID;

/** Spring Data repository for {@link TimeSheet}. */
public interface TimeSheetRepository
        extends JpaRepository<TimeSheet, UUID>, JpaSpecificationExecutor<TimeSheet> {
}
