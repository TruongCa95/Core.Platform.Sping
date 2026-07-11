package vn.aequitas.coreplatform.application.timesheet.command.createstudent;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.aequitas.coreplatform.application.common.exception.DuplicateNameException;
import vn.aequitas.coreplatform.application.common.mediator.RequestHandler;
import vn.aequitas.coreplatform.domain.entity.timesheet.StudentClasses;
import vn.aequitas.coreplatform.domain.entity.timesheet.Students;
import vn.aequitas.coreplatform.domain.repository.UnitOfWork;

import java.util.List;
import java.util.UUID;

/** Port of the .NET {@code CreateStudentCommandHandler}. */
@Component
public class CreateStudentCommandHandler implements RequestHandler<CreateStudentCommand, UUID> {

    private final UnitOfWork unitOfWork;

    public CreateStudentCommandHandler(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    @Transactional
    public UUID handle(CreateStudentCommand request) {
        boolean exists = unitOfWork.students()
                .getOne((root, query, cb) -> cb.and(
                        cb.equal(root.get("isActive"), true),
                        cb.equal(root.get("name"), request.getName())))
                .isPresent();
        if (exists) {
            throw new DuplicateNameException();
        }

        Students student = new Students();
        student.setGrade(request.getGrade());
        student.setReview(request.getReview());
        student.setName(request.getName());

        unitOfWork.students().add(student);

        List<StudentClasses> relationships = request.getClassroomIds().stream()
                .map(classId -> {
                    StudentClasses sc = new StudentClasses();
                    sc.setStudentId(student.getId());
                    sc.setClassId(classId);
                    return sc;
                })
                .toList();

        unitOfWork.studentClasses().addRange(relationships);
        unitOfWork.complete();
        return student.getId();
    }
}
