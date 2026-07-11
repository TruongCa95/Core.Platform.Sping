package vn.aequitas.coreplatform.application.timesheet.command.deletetimesheet;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.aequitas.coreplatform.application.common.mediator.RequestHandler;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoomTimeSheet;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimeSheet;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimesheetReview;
import vn.aequitas.coreplatform.domain.repository.UnitOfWork;

import java.util.List;
import java.util.Optional;

/**
 * Port of the .NET {@code DeleteTimesheetByIdCommandHandler}. Hard-deletes the
 * timesheet and its related join / review rows. Note the original does NOT filter
 * on {@code IsActive} when locating the timesheet.
 */
@Component
public class DeleteTimesheetByIdCommandHandler implements RequestHandler<DeleteTimesheetByIdCommand, Boolean> {

    private final UnitOfWork unitOfWork;

    public DeleteTimesheetByIdCommandHandler(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    @Transactional
    public Boolean handle(DeleteTimesheetByIdCommand request) {
        Optional<TimeSheet> found = unitOfWork.timeSheets()
                .getOne((root, query, cb) -> cb.equal(root.get("id"), request.getId()));
        if (found.isEmpty()) {
            return false;
        }
        TimeSheet timesheet = found.get();

        List<ClassRoomTimeSheet> relationships = unitOfWork.classRoomTimeSheets()
                .getListByCondition((root, query, cb) -> cb.equal(root.get("timeSheetId"), request.getId()));
        if (!relationships.isEmpty()) {
            unitOfWork.classRoomTimeSheets()
                    .deleteByIds(relationships.stream().map(ClassRoomTimeSheet::getId).toList());
        }

        List<TimesheetReview> reviews = unitOfWork.timesheetReviews()
                .getListByCondition((root, query, cb) -> cb.equal(root.get("timesheetId"), request.getId()));
        if (!reviews.isEmpty()) {
            unitOfWork.timesheetReviews()
                    .deleteByIds(reviews.stream().map(TimesheetReview::getId).toList());
        }

        unitOfWork.timeSheets().deleteById(timesheet.getId());
        unitOfWork.complete();
        return true;
    }
}
