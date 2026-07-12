package vn.aequitas.coreplatform.application.timesheet.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.aequitas.coreplatform.application.common.dto.PagedResult;
import vn.aequitas.coreplatform.application.timesheet.query.getliststudent.GetListStudentQueryResult;
import vn.aequitas.coreplatform.domain.entity.timesheet.StudentClasses;
import vn.aequitas.coreplatform.domain.entity.timesheet.Students;
import vn.aequitas.coreplatform.domain.repository.StudentClassesRepository;
import vn.aequitas.coreplatform.domain.repository.StudentsRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Read-side operations for students. Port of the former
 * {@code GetListStudentQueryHandler}.
 */
@Service
public class StudentQueryService {

    private final StudentsRepository students;
    private final StudentClassesRepository studentClasses;

    public StudentQueryService(StudentsRepository students, StudentClassesRepository studentClasses) {
        this.students = students;
        this.studentClasses = studentClasses;
    }

    @Transactional(readOnly = true)
    public PagedResult<GetListStudentQueryResult> getList(int page, int pageSize, String search) {
        Specification<Students> spec = (root, query, cb) -> cb.equal(root.get("isActive"), true);

        if (StringUtils.hasText(search)) {
            String like = "%" + search.trim() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(root.<String>get("name"), like),
                    cb.like(root.<String>get("grade"), like),
                    cb.like(root.<String>get("review"), like)));
        }

        long totalCount = students.count(spec);
        int resolvedPage = page <= 0 ? 1 : page;
        int resolvedPageSize = pageSize <= 0 ? 20 : pageSize;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");

        List<Students> rows = students
                .findAll(spec, PageRequest.of(resolvedPage - 1, resolvedPageSize, sort))
                .getContent();

        // Load active enrolments for the students on this page so the client can
        // pre-populate the "classes" field when editing.
        List<UUID> studentIds = rows.stream().map(Students::getId).toList();
        List<StudentClasses> enrolments = studentClasses.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("isActive"), true),
                root.get("studentId").in(studentIds)));
        Map<UUID, List<UUID>> classIdsByStudent = enrolments.stream()
                .collect(Collectors.groupingBy(StudentClasses::getStudentId,
                        Collectors.mapping(StudentClasses::getClassId, Collectors.toList())));

        List<GetListStudentQueryResult> items = rows.stream()
                .map(s -> GetListStudentQueryResult.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .grade(s.getGrade())
                        .review(s.getReview())
                        .classroomIds(classIdsByStudent.getOrDefault(s.getId(), List.of()))
                        .build())
                .toList();

        return PagedResult.<GetListStudentQueryResult>builder()
                .items(items)
                .page(resolvedPage)
                .pageSize(resolvedPageSize)
                .totalCount((int) totalCount)
                .totalPages((int) Math.ceil(totalCount / (double) resolvedPageSize))
                .build();
    }
}
