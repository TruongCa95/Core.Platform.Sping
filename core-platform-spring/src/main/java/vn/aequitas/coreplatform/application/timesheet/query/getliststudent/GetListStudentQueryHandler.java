package vn.aequitas.coreplatform.application.timesheet.query.getliststudent;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.aequitas.coreplatform.application.common.dto.PagedResult;
import vn.aequitas.coreplatform.application.common.mediator.RequestHandler;
import vn.aequitas.coreplatform.domain.entity.timesheet.StudentClasses;
import vn.aequitas.coreplatform.domain.entity.timesheet.Students;
import vn.aequitas.coreplatform.domain.repository.UnitOfWork;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Port of the .NET {@code GetListStudentQueryHandler}. */
@Component
public class GetListStudentQueryHandler
        implements RequestHandler<GetListStudentQuery, PagedResult<GetListStudentQueryResult>> {

    private final UnitOfWork unitOfWork;

    public GetListStudentQueryHandler(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<GetListStudentQueryResult> handle(GetListStudentQuery request) {
        Specification<Students> spec = (root, query, cb) -> cb.equal(root.get("isActive"), true);

        if (StringUtils.hasText(request.getSearch())) {
            String like = "%" + request.getSearch().trim() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(root.<String>get("name"), like),
                    cb.like(root.<String>get("grade"), like),
                    cb.like(root.<String>get("review"), like)));
        }

        long totalCount = unitOfWork.students().count(spec);
        int page = request.getPage() <= 0 ? 1 : request.getPage();
        int pageSize = request.getPageSize() <= 0 ? 20 : request.getPageSize();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");

        List<Students> students = unitOfWork.students().getPaged(spec, sort, page, pageSize);

        // Load active enrolments for the students on this page so the client can
        // pre-populate the "classes" field when editing.
        List<java.util.UUID> studentIds = students.stream().map(Students::getId).toList();
        List<StudentClasses> enrolments = unitOfWork.studentClasses()
                .getListByCondition((root, query, cb) -> cb.and(
                        cb.equal(root.get("isActive"), true),
                        root.get("studentId").in(studentIds)));
        Map<java.util.UUID, List<java.util.UUID>> classIdsByStudent = enrolments.stream()
                .collect(Collectors.groupingBy(StudentClasses::getStudentId,
                        Collectors.mapping(StudentClasses::getClassId, Collectors.toList())));

        List<GetListStudentQueryResult> items = students.stream()
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
                .page(page)
                .pageSize(pageSize)
                .totalCount((int) totalCount)
                .totalPages((int) Math.ceil(totalCount / (double) pageSize))
                .build();
    }
}
