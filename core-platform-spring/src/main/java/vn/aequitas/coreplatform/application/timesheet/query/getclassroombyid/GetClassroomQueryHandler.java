package vn.aequitas.coreplatform.application.timesheet.query.getclassroombyid;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.aequitas.coreplatform.application.common.mediator.RequestHandler;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoom;
import vn.aequitas.coreplatform.domain.repository.UnitOfWork;

/**
 * Port of the .NET {@code GetClassroomQueryHandler}. {@code getById} throws
 * {@code NotFoundException} (HTTP 404) when the classroom does not exist.
 */
@Component
public class GetClassroomQueryHandler implements RequestHandler<GetClassroomQuery, GetClassroomQueryResult> {

    private final UnitOfWork unitOfWork;

    public GetClassroomQueryHandler(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    @Transactional(readOnly = true)
    public GetClassroomQueryResult handle(GetClassroomQuery request) {
        ClassRoom classroom = unitOfWork.classrooms().getById(request.getClassroomId());

        return GetClassroomQueryResult.builder()
                .id(classroom.getId())
                .classCode(classroom.getClassCode())
                .className(classroom.getClassName())
                .numberOfStudent(classroom.getNumberOfStudent())
                .status(classroom.getStatus().getValue())
                .build();
    }
}
