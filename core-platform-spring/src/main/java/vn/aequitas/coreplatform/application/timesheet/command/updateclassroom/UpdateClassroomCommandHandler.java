package vn.aequitas.coreplatform.application.timesheet.command.updateclassroom;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.aequitas.coreplatform.application.common.exception.DuplicateNameException;
import vn.aequitas.coreplatform.application.common.mediator.RequestHandler;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoom;
import vn.aequitas.coreplatform.domain.repository.UnitOfWork;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/** Port of the .NET {@code UpdateClassroomCommandHandler}. */
@Component
public class UpdateClassroomCommandHandler implements RequestHandler<UpdateClassroomCommand, Boolean> {

    private final UnitOfWork unitOfWork;

    public UpdateClassroomCommandHandler(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    @Transactional
    public Boolean handle(UpdateClassroomCommand request) {
        Optional<ClassRoom> found = unitOfWork.classrooms()
                .getOne((root, query, cb) -> cb.and(
                        cb.equal(root.get("isActive"), true),
                        cb.equal(root.get("id"), request.getId())));
        if (found.isEmpty()) {
            return false;
        }
        ClassRoom classroom = found.get();

        // When the class code changes, make sure no other active classroom already uses it.
        if (StringUtils.hasText(request.getClassCode())
                && !request.getClassCode().equals(classroom.getClassCode())) {
            boolean duplicate = unitOfWork.classrooms()
                    .getOne((root, query, cb) -> cb.and(
                            cb.equal(root.get("isActive"), true),
                            cb.notEqual(root.get("id"), request.getId()),
                            cb.equal(root.get("classCode"), request.getClassCode())))
                    .isPresent();
            if (duplicate) {
                throw new DuplicateNameException();
            }
            classroom.setClassCode(request.getClassCode());
        }

        classroom.setClassName(request.getClassName() != null ? request.getClassName() : "");
        classroom.setLocation(request.getLocation() != null ? request.getLocation() : "");
        classroom.setNumberOfStudent(request.getNumberOfStudent());
        classroom.setLevel(request.getLevel());
        classroom.setStatus(request.getStatus());
        classroom.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));

        unitOfWork.classrooms().update(classroom);
        unitOfWork.complete();
        return true;
    }
}
