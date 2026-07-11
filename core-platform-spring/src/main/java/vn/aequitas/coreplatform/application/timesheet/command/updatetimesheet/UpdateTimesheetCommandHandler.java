package vn.aequitas.coreplatform.application.timesheet.command.updatetimesheet;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.aequitas.coreplatform.application.common.mediator.RequestHandler;
import vn.aequitas.coreplatform.application.timesheet.helper.TimeHelper;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoomTimeSheet;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimeSheet;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimesheetReview;
import vn.aequitas.coreplatform.domain.repository.UnitOfWork;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/** Port of the .NET {@code UpdateTimesheetCommandHandler}. */
@Component
public class UpdateTimesheetCommandHandler implements RequestHandler<UpdateTimesheetCommand, Boolean> {

    private final UnitOfWork unitOfWork;

    public UpdateTimesheetCommandHandler(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    @Transactional
    public Boolean handle(UpdateTimesheetCommand request) {
        Optional<TimeSheet> found = unitOfWork.timeSheets()
                .getOne((root, query, cb) -> cb.and(
                        cb.equal(root.get("isActive"), true),
                        cb.equal(root.get("id"), request.getId())));
        if (found.isEmpty()) {
            return false;
        }
        TimeSheet timesheet = found.get();

        timesheet.setUpdatedDate(LocalDateTime.now(ZoneOffset.UTC));
        timesheet.setDescription(request.getDescription());
        timesheet.setDate(request.getDate());
        timesheet.setName(TimeHelper.generateTimesheetName(request.getDate()));
        unitOfWork.timeSheets().update(timesheet);

        // Replace the classroom links when the caller supplies them.
        if (request.getClassrooms() != null && !request.getClassrooms().isEmpty()) {
            List<ClassRoomTimeSheet> existingRelationships = unitOfWork.classRoomTimeSheets()
                    .getListByCondition((root, query, cb) -> cb.equal(root.get("timeSheetId"), request.getId()));
            if (!existingRelationships.isEmpty()) {
                unitOfWork.classRoomTimeSheets()
                        .deleteByIds(existingRelationships.stream().map(ClassRoomTimeSheet::getId).toList());
            }

            List<ClassRoomTimeSheet> relationships = request.getClassrooms().stream()
                    .map(classroom -> {
                        ClassRoomTimeSheet link = new ClassRoomTimeSheet();
                        link.setTimeSheetId(timesheet.getId());
                        link.setClassRoomId(classroom.getClassroomId());
                        link.setNumberOfStudent(classroom.getNumberOfStudent());
                        return link;
                    })
                    .toList();
            unitOfWork.classRoomTimeSheets().addRange(relationships);
        }

        // Replace the reviews with the set from the form (add / edit / remove).
        List<TimesheetReview> existingReviews = unitOfWork.timesheetReviews()
                .getListByCondition((root, query, cb) -> cb.equal(root.get("timesheetId"), request.getId()));
        if (!existingReviews.isEmpty()) {
            unitOfWork.timesheetReviews()
                    .deleteByIds(existingReviews.stream().map(TimesheetReview::getId).toList());
        }

        if (request.getTimesheetReviews() != null && !request.getTimesheetReviews().isEmpty()) {
            List<TimesheetReview> reviews = request.getTimesheetReviews().stream()
                    .map(review -> {
                        TimesheetReview entity = new TimesheetReview();
                        entity.setProgress(review.getProgress());
                        entity.setReview(review.getReview());
                        entity.setStudentId(review.getStudentId());
                        entity.setTimesheetId(timesheet.getId());
                        return entity;
                    })
                    .toList();
            unitOfWork.timesheetReviews().addRange(reviews);
        }

        unitOfWork.complete();
        return true;
    }
}
