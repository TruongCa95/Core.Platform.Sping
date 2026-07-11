package vn.aequitas.coreplatform.application.timesheet.query.getlistclassroom;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.aequitas.coreplatform.application.common.dto.PagedResult;
import vn.aequitas.coreplatform.application.common.mediator.RequestHandler;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoom;
import vn.aequitas.coreplatform.domain.repository.UnitOfWork;

import java.util.List;

/** Port of the .NET {@code GetListClassroomQueryHandler}. */
@Component
public class GetListClassroomQueryHandler
        implements RequestHandler<GetListClassroomQuery, PagedResult<GetListClassroomQueryResult>> {

    private final UnitOfWork unitOfWork;

    public GetListClassroomQueryHandler(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<GetListClassroomQueryResult> handle(GetListClassroomQuery request) {
        Specification<ClassRoom> spec = (root, query, cb) -> cb.equal(root.get("isActive"), true);

        if (StringUtils.hasText(request.getSearch())) {
            String like = "%" + request.getSearch().trim() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(root.<String>get("className"), like),
                    cb.like(root.<String>get("classCode"), like),
                    cb.like(root.<String>get("location"), like)));
        }

        long totalCount = unitOfWork.classrooms().count(spec);
        int page = request.getPage() <= 0 ? 1 : request.getPage();
        int pageSize = request.getPageSize() <= 0 ? 20 : request.getPageSize();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");

        List<ClassRoom> classrooms = unitOfWork.classrooms().getPaged(spec, sort, page, pageSize);

        List<GetListClassroomQueryResult> items = classrooms.stream()
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
                .page(page)
                .pageSize(pageSize)
                .totalCount((int) totalCount)
                .totalPages((int) Math.ceil(totalCount / (double) pageSize))
                .build();
    }
}
