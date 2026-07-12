package vn.aequitas.coreplatform.application.timesheet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.aequitas.coreplatform.application.common.exception.DuplicateNameException;
import vn.aequitas.coreplatform.application.timesheet.command.createstudent.CreateStudentCommand;
import vn.aequitas.coreplatform.application.timesheet.command.updatestudent.UpdateStudentCommand;
import vn.aequitas.coreplatform.domain.entity.timesheet.StudentClasses;
import vn.aequitas.coreplatform.domain.entity.timesheet.Students;
import vn.aequitas.coreplatform.domain.repository.StudentClassesRepository;
import vn.aequitas.coreplatform.domain.repository.StudentsRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Write-side operations for students. Consolidates the former
 * {@code CreateStudentCommandHandler}, {@code UpdateStudentCommandHandler} and
 * {@code DeleteStudentByIdCommandHandler}.
 */
@Service
public class StudentCommandService {

    private final StudentsRepository students;
    private final StudentClassesRepository studentClasses;

    public StudentCommandService(StudentsRepository students, StudentClassesRepository studentClasses) {
        this.students = students;
        this.studentClasses = studentClasses;
    }

    @Transactional
    public UUID create(CreateStudentCommand request) {
        boolean exists = students.findOne((root, query, cb) -> cb.and(
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
        students.save(student);

        List<StudentClasses> relationships = request.getClassroomIds().stream()
                .map(classId -> {
                    StudentClasses sc = new StudentClasses();
                    sc.setStudentId(student.getId());
                    sc.setClassId(classId);
                    return sc;
                })
                .toList();
        studentClasses.saveAll(relationships);
        return student.getId();
    }

    @Transactional
    public boolean update(UpdateStudentCommand request) {
        Optional<Students> found = students.findOne((root, query, cb) -> cb.and(
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
        students.save(student);

        // Reconcile classroom enrolments only when the caller sends the list.
        if (request.getClassroomIds() != null) {
            reconcileEnrolments(student.getId(), request.getClassroomIds());
        }
        return true;
    }

    private void reconcileEnrolments(UUID studentId, List<UUID> desiredClassIds) {
        List<UUID> desired = desiredClassIds.stream().distinct().toList();

        // Load every enrolment row (active or not) so soft-deleted ones can be reactivated
        // instead of creating duplicates.
        List<StudentClasses> existing = studentClasses.findAll(
                (root, query, cb) -> cb.equal(root.get("studentId"), studentId));

        List<StudentClasses> toUpdate = new ArrayList<>();
        for (StudentClasses enrolment : existing) {
            boolean shouldBeActive = desired.contains(enrolment.getClassId());
            if (enrolment.isActive() != shouldBeActive) {
                enrolment.setActive(shouldBeActive);
                enrolment.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
                toUpdate.add(enrolment);
            }
        }
        if (!toUpdate.isEmpty()) {
            studentClasses.saveAll(toUpdate);
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
            studentClasses.saveAll(toAdd);
        }
    }

    @Transactional
    public boolean delete(UUID id) {
        Optional<Students> found = students.findOne((root, query, cb) -> cb.and(
                cb.equal(root.get("isActive"), true),
                cb.equal(root.get("id"), id)));
        if (found.isEmpty()) {
            return false;
        }
        Students student = found.get();

        student.setActive(false);
        student.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
        students.save(student);

        // Soft-delete the student's classroom enrolments.
        List<StudentClasses> enrolments = studentClasses.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("isActive"), true),
                cb.equal(root.get("studentId"), id)));
        for (StudentClasses enrolment : enrolments) {
            enrolment.setActive(false);
            enrolment.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
        }
        studentClasses.saveAll(enrolments);
        return true;
    }
}
