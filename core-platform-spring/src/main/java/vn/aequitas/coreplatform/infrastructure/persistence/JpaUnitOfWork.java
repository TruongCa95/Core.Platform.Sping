package vn.aequitas.coreplatform.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import vn.aequitas.coreplatform.domain.common.BaseEntity;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoom;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoomTimeSheet;
import vn.aequitas.coreplatform.domain.entity.timesheet.Salary;
import vn.aequitas.coreplatform.domain.entity.timesheet.StudentClasses;
import vn.aequitas.coreplatform.domain.entity.timesheet.Students;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimeSheet;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimesheetReview;
import vn.aequitas.coreplatform.domain.repository.Repository;
import vn.aequitas.coreplatform.domain.repository.UnitOfWork;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Port of the .NET {@code UnitOfWork}. Exposes one generic {@link Repository} per
 * entity (built lazily over the shared, transaction-bound {@link EntityManager})
 * and flushes on {@link #complete()}.
 *
 * <p>The injected {@code EntityManager} is a container-managed proxy that resolves
 * to the current transaction's persistence context, so a single instance is safe
 * to share; the per-entity repositories are stateless wrappers cached here.</p>
 */
@Component
public class JpaUnitOfWork implements UnitOfWork {

    @PersistenceContext
    private EntityManager entityManager;

    private final Map<Class<?>, Repository<?>> repositories = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private <T extends BaseEntity> Repository<T> repositoryFor(Class<T> entityClass) {
        return (Repository<T>) repositories.computeIfAbsent(entityClass,
                key -> new JpaRepositoryImpl<>((Class<T>) key, entityManager));
    }

    @Override
    public Repository<TimeSheet> timeSheets() {
        return repositoryFor(TimeSheet.class);
    }

    @Override
    public Repository<ClassRoom> classrooms() {
        return repositoryFor(ClassRoom.class);
    }

    @Override
    public Repository<ClassRoomTimeSheet> classRoomTimeSheets() {
        return repositoryFor(ClassRoomTimeSheet.class);
    }

    @Override
    public Repository<Students> students() {
        return repositoryFor(Students.class);
    }

    @Override
    public Repository<Salary> salaries() {
        return repositoryFor(Salary.class);
    }

    @Override
    public Repository<TimesheetReview> timesheetReviews() {
        return repositoryFor(TimesheetReview.class);
    }

    @Override
    public Repository<StudentClasses> studentClasses() {
        return repositoryFor(StudentClasses.class);
    }

    @Override
    public int complete() {
        entityManager.flush();
        return 0;
    }
}
