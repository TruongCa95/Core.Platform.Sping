package vn.aequitas.coreplatform.application.timesheet.command.deleteclassroom;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.aequitas.coreplatform.application.common.mediator.RequestHandler;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoom;
import vn.aequitas.coreplatform.domain.entity.timesheet.StudentClasses;
import vn.aequitas.coreplatform.domain.repository.UnitOfWork;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/** Port of the .NET {@code DeleteClassroomByIdCommandHandler} (soft delete). */
@Component
public class DeleteClassroomByIdCommandHandler implements RequestHandler<DeleteClassroomByIdCommand, Boolean> {

    private final UnitOfWork unitOfWork;

    public DeleteClassroomByIdCommandHandler(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    @Transactional
    public Boolean handle(DeleteClassroomByIdCommand request) {
        Optional<ClassRoom> found = unitOfWork.classrooms()
                .getOne((root, query, cb) -> cb.and(
                        cb.equal(root.get("isActive"), true),
                        cb.equal(root.get("id"), request.getId())));
        if (found.isEmpty()) {
            return false;
        }
        ClassRoom classroom = found.get();

        classroom.setActive(false);
        classroom.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
        unitOfWork.classrooms().update(classroom);

        // Soft-delete the enrolments that reference this classroom.
        List<StudentClasses> enrolments = unitOfWork.studentClasses()
                .getListByCondition((root, query, cb) -> cb.and(
                        cb.equal(root.get("isActive"), true),
                        cb.equal(root.get("classId"), request.getId())));
        for (StudentClasses enrolment : enrolments) {
            enrolment.setActive(false);
            enrolment.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
            unitOfWork.studentClasses().update(enrolment);
        }

        unitOfWork.complete();
        return true;
    }
}
