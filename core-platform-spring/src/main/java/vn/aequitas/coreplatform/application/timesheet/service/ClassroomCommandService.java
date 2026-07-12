package vn.aequitas.coreplatform.application.timesheet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.aequitas.coreplatform.application.common.exception.DuplicateNameException;
import vn.aequitas.coreplatform.application.timesheet.command.createclassroom.CreateClassroomCommand;
import vn.aequitas.coreplatform.application.timesheet.command.updateclassroom.UpdateClassroomCommand;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoom;
import vn.aequitas.coreplatform.domain.entity.timesheet.StudentClasses;
import vn.aequitas.coreplatform.domain.repository.ClassRoomRepository;
import vn.aequitas.coreplatform.domain.repository.StudentClassesRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Write-side operations for classrooms. Consolidates the former
 * {@code CreateClassroomCommandHandler}, {@code UpdateClassroomCommandHandler} and
 * {@code DeleteClassroomByIdCommandHandler}.
 */
@Service
public class ClassroomCommandService {

    private final ClassRoomRepository classrooms;
    private final StudentClassesRepository studentClasses;

    public ClassroomCommandService(ClassRoomRepository classrooms, StudentClassesRepository studentClasses) {
        this.classrooms = classrooms;
        this.studentClasses = studentClasses;
    }

    @Transactional
    public UUID create(CreateClassroomCommand request) {
        boolean exists = classrooms.findOne((root, query, cb) -> cb.and(
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

        classrooms.save(classroom);
        return classroom.getId();
    }

    @Transactional
    public boolean update(UpdateClassroomCommand request) {
        Optional<ClassRoom> found = classrooms.findOne((root, query, cb) -> cb.and(
                cb.equal(root.get("isActive"), true),
                cb.equal(root.get("id"), request.getId())));
        if (found.isEmpty()) {
            return false;
        }
        ClassRoom classroom = found.get();

        // When the class code changes, make sure no other active classroom already uses it.
        if (StringUtils.hasText(request.getClassCode())
                && !request.getClassCode().equals(classroom.getClassCode())) {
            boolean duplicate = classrooms.findOne((root, query, cb) -> cb.and(
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

        classrooms.save(classroom);
        return true;
    }

    @Transactional
    public boolean delete(UUID id) {
        Optional<ClassRoom> found = classrooms.findOne((root, query, cb) -> cb.and(
                cb.equal(root.get("isActive"), true),
                cb.equal(root.get("id"), id)));
        if (found.isEmpty()) {
            return false;
        }
        ClassRoom classroom = found.get();

        classroom.setActive(false);
        classroom.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
        classrooms.save(classroom);

        // Soft-delete the enrolments that reference this classroom.
        List<StudentClasses> enrolments = studentClasses.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("isActive"), true),
                cb.equal(root.get("classId"), id)));
        for (StudentClasses enrolment : enrolments) {
            enrolment.setActive(false);
            enrolment.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
        }
        studentClasses.saveAll(enrolments);
        return true;
    }
}
