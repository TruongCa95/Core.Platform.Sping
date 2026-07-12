package vn.aequitas.coreplatform.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimesheetReview;

import java.util.UUID;

/** Spring Data repository for {@link TimesheetReview}. */
public interface TimesheetReviewRepository
        extends JpaRepository<TimesheetReview, UUID>, JpaSpecificationExecutor<TimesheetReview> {
}
