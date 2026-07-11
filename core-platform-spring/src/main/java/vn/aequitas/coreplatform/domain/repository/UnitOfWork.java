package vn.aequitas.coreplatform.domain.repository;

import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoom;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoomTimeSheet;
import vn.aequitas.coreplatform.domain.entity.timesheet.Salary;
import vn.aequitas.coreplatform.domain.entity.timesheet.StudentClasses;
import vn.aequitas.coreplatform.domain.entity.timesheet.Students;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimeSheet;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimesheetReview;

/**
 * Aggregates the per-entity repositories behind a single transactional boundary,
 * mirroring the .NET {@code IUnitOfWork}. {@link #complete()} flushes pending
 * changes (the counterpart of {@code CompleteAsync}); atomicity is provided by the
 * {@code @Transactional} handler methods that call these repositories.
 */
public interface UnitOfWork {

    Repository<TimeSheet> timeSheets();

    Repository<ClassRoom> classrooms();

    Repository<ClassRoomTimeSheet> classRoomTimeSheets();

    Repository<Students> students();

    Repository<Salary> salaries();

    Repository<TimesheetReview> timesheetReviews();

    Repository<StudentClasses> studentClasses();

    int complete();
}
