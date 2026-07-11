package vn.aequitas.coreplatform.application.timesheet.command.deletestudent;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.aequitas.coreplatform.application.common.mediator.RequestHandler;
import vn.aequitas.coreplatform.domain.entity.timesheet.StudentClasses;
import vn.aequitas.coreplatform.domain.entity.timesheet.Students;
import vn.aequitas.coreplatform.domain.repository.UnitOfWork;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/** Port of the .NET {@code DeleteStudentByIdCommandHandler} (soft delete). */
@Component
public class DeleteStudentByIdCommandHandler implements RequestHandler<DeleteStudentByIdCommand, Boolean> {

    private final UnitOfWork unitOfWork;

    public DeleteStudentByIdCommandHandler(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    @Transactional
    public Boolean handle(DeleteStudentByIdCommand request) {
        Optional<Students> found = unitOfWork.students()
                .getOne((root, query, cb) -> cb.and(
                        cb.equal(root.get("isActive"), true),
                        cb.equal(root.get("id"), request.getId())));
        if (found.isEmpty()) {
            return false;
        }
        Students student = found.get();

        student.setActive(false);
        student.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
        unitOfWork.students().update(student);

        // Soft-delete the student's classroom enrolments.
        List<StudentClasses> enrolments = unitOfWork.studentClasses()
                .getListByCondition((root, query, cb) -> cb.and(
                        cb.equal(root.get("isActive"), true),
                        cb.equal(root.get("studentId"), request.getId())));
        for (StudentClasses enrolment : enrolments) {
            enrolment.setActive(false);
            enrolment.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
            unitOfWork.studentClasses().update(enrolment);
        }

        unitOfWork.complete();
        return true;
    }
}
