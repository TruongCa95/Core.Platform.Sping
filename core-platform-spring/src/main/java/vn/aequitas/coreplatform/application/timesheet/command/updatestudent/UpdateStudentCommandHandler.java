package vn.aequitas.coreplatform.application.timesheet.command.updatestudent;

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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Port of the .NET {@code UpdateStudentCommandHandler}. */
@Component
public class UpdateStudentCommandHandler implements RequestHandler<UpdateStudentCommand, Boolean> {

    private final UnitOfWork unitOfWork;

    public UpdateStudentCommandHandler(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    @Transactional
    public Boolean handle(UpdateStudentCommand request) {
        Optional<Students> found = unitOfWork.students()
                .getOne((root, query, cb) -> cb.and(
                        cb.equal(root.get("isActive"), true),
                        cb.equal(root.get("id"), request.getId())));
        if (found.isEmpty()) {
            return false;
        }
        Students student = found.get();

        student.setName(request.getName() != null ? request.getName() : "");
        student.setGrade(request.getGrade() != null ? request.getGrade() : "");
        student.setReview(request.getReview() != null ? request.getReview() : "");
        student.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
        unitOfWork.students().update(student);

        // Reconcile classroom enrolments only when the caller sends the list.
        if (request.getClassroomIds() != null) {
            reconcileEnrolments(student.getId(), request.getClassroomIds());
        }

        unitOfWork.complete();
        return true;
    }

    private void reconcileEnrolments(UUID studentId, List<UUID> desiredClassIds) {
        List<UUID> desired = desiredClassIds.stream().distinct().toList();

        // Load every enrolment row (active or not) so soft-deleted ones can be reactivated
        // instead of creating duplicates.
        List<StudentClasses> existing = unitOfWork.studentClasses()
                .getListByCondition((root, query, cb) -> cb.equal(root.get("studentId"), studentId));

        for (StudentClasses enrolment : existing) {
            boolean shouldBeActive = desired.contains(enrolment.getClassId());
            if (enrolment.isActive() != shouldBeActive) {
                enrolment.setActive(shouldBeActive);
                enrolment.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
                unitOfWork.studentClasses().update(enrolment);
            }
        }

        Set<UUID> existingClassIds = existing.stream()
                .map(StudentClasses::getClassId)
                .collect(Collectors.toSet());
        List<StudentClasses> toAdd = desired.stream()
                .filter(classId -> !existingClassIds.contains(classId))
                .map(classId -> {
                    StudentClasses sc = new StudentClasses();
                    sc.setStudentId(studentId);
                    sc.setClassId(classId);
                    return sc;
                })
                .toList();

        if (!toAdd.isEmpty()) {
            unitOfWork.studentClasses().addRange(toAdd);
        }
    }
}
