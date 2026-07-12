package vn.aequitas.coreplatform.application.timesheet.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.aequitas.coreplatform.application.common.dto.PagedResult;
import vn.aequitas.coreplatform.application.common.exception.NotFoundException;
import vn.aequitas.coreplatform.application.timesheet.query.getclassroombyid.GetClassroomQueryResult;
import vn.aequitas.coreplatform.application.timesheet.query.getlistclassroom.GetListClassroomQueryResult;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoom;
import vn.aequitas.coreplatform.domain.repository.ClassRoomRepository;

import java.util.List;
import java.util.UUID;

/**
 * Read-side operations for classrooms. Consolidates the former
 * {@code GetClassroomQueryHandler} and {@code GetListClassroomQueryHandler}.
 */
@Service
public class ClassroomQueryService {

    private final ClassRoomRepository classrooms;

    public ClassroomQueryService(ClassRoomRepository classrooms) {
        this.classrooms = classrooms;
    }

    @Transactional(readOnly = true)
    public GetClassroomQueryResult getById(UUID classroomId) {
        ClassRoom classroom = classrooms.findById(classroomId)
                .orElseThrow(() -> new NotFoundException("Entity with ID " + classroomId + " not found."));

        return GetClassroomQueryResult.builder()
                .id(classroom.getId())
                .classCode(classroom.getClassCode())
                .className(classroom.getClassName())
                .numberOfStudent(classroom.getNumberOfStudent())
                .status(classroom.getStatus().getValue())
                .build();
    }

    @Transactional(readOnly = true)
    public PagedResult<GetListClassroomQueryResult> getList(int page, int pageSize, String search) {
        Specification<ClassRoom> spec = (root, query, cb) -> cb.equal(root.get("isActive"), true);

        if (StringUtils.hasText(search)) {
            String like = "%" + search.trim() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(root.<String>get("className"), like),
                    cb.like(root.<String>get("classCode"), like),
                    cb.like(root.<String>get("location"), like)));
        }

        long totalCount = classrooms.count(spec);
        int resolvedPage = page <= 0 ? 1 : page;
        int resolvedPageSize = pageSize <= 0 ? 20 : pageSize;
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");

        List<ClassRoom> rows = classrooms
                .findAll(spec, PageRequest.of(resolvedPage - 1, resolvedPageSize, sort))
                .getContent();

        List<GetListClassroomQueryResult> items = rows.stream()
                .map(x -> GetListClassroomQueryResult.builder()
                        .id(x.getId())
                        .classCode(x.getClassCode())
                        .className(x.getClassName())
                        .location(x.getLocation())
                        .numberOfStudent(x.getNumberOfStudent())
                        .level(x.getLevel().getValue())
                        .status(x.getStatus().getValue())
                        .build())
                .toList();

        return PagedResult.<GetListClassroomQueryResult>builder()
                .items(items)
                .page(resolvedPage)
                .pageSize(resolvedPageSize)
                .totalCount((int) totalCount)
                .totalPages((int) Math.ceil(totalCount / (double) resolvedPageSize))
                .build();
    }
}
