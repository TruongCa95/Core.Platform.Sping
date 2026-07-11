package vn.aequitas.coreplatform.application.timesheet.command.createclassroom;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.aequitas.coreplatform.application.common.exception.DuplicateNameException;
import vn.aequitas.coreplatform.application.common.mediator.RequestHandler;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoom;
import vn.aequitas.coreplatform.domain.repository.UnitOfWork;

import java.util.UUID;

/** Port of the .NET {@code CreateClassroomCommandHandler}. */
@Component
public class CreateClassroomCommandHandler implements RequestHandler<CreateClassroomCommand, UUID> {

    private final UnitOfWork unitOfWork;

    public CreateClassroomCommandHandler(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    @Transactional
    public UUID handle(CreateClassroomCommand request) {
        boolean exists = unitOfWork.classrooms()
                .getOne((root, query, cb) -> cb.and(
                        cb.equal(root.get("isActive"), true),
                        cb.equal(root.get("classCode"), request.getClassCode())))
                .isPresent();
        if (exists) {
            throw new DuplicateNameException();
        }

        ClassRoom classroom = new ClassRoom();
        classroom.setId(UUID.randomUUID());
        classroom.setClassCode(request.getClassCode());
        classroom.setClassName(request.getClassName());
        classroom.setNumberOfStudent(request.getNumberOfStudent());
        classroom.setLocation(request.getLocation());
        classroom.setLevel(request.getLevel());
        classroom.setStatus(request.getStatus());

        unitOfWork.classrooms().add(classroom);
        unitOfWork.complete();
        return classroom.getId();
    }
}
