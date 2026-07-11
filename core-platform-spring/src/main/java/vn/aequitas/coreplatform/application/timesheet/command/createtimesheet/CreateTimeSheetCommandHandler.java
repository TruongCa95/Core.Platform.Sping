package vn.aequitas.coreplatform.application.timesheet.command.createtimesheet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.aequitas.coreplatform.application.common.mediator.RequestHandler;
import vn.aequitas.coreplatform.application.timesheet.helper.TimeHelper;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoomTimeSheet;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimeSheet;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimesheetReview;
import vn.aequitas.coreplatform.domain.repository.UnitOfWork;

import java.util.List;
import java.util.UUID;

/** Port of the .NET {@code CreateTimeSheetCommandHandler}. */
@Component
public class CreateTimeSheetCommandHandler implements RequestHandler<CreateTimesheetCommand, UUID> {

    private static final Logger log = LoggerFactory.getLogger(CreateTimeSheetCommandHandler.class);

    private final UnitOfWork unitOfWork;

    public CreateTimeSheetCommandHandler(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    @Transactional
    public UUID handle(CreateTimesheetCommand request) {
        try {
            TimeSheet timesheet = new TimeSheet();
            timesheet.setId(UUID.randomUUID());
            timesheet.setName(TimeHelper.generateTimesheetName(request.getDate()));
            timesheet.setDate(request.getDate());
            timesheet.setDescription(request.getDescription());

            List<ClassRoomTimeSheet> relationships = request.getClassrooms().stream()
                    .map(classroom -> {
                        ClassRoomTimeSheet link = new ClassRoomTimeSheet();
                        link.setTimeSheetId(timesheet.getId());
                        link.setClassRoomId(classroom.getClassroomId());
                        link.setNumberOfStudent(classroom.getNumberOfStudent());
                        return link;
                    })
                    .toList();

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

            unitOfWork.timeSheets().add(timesheet);
            unitOfWork.classRoomTimeSheets().addRange(relationships);
            unitOfWork.timesheetReviews().addRange(reviews);
            unitOfWork.complete();
            return timesheet.getId();
        } catch (Exception ex) {
            log.error("Error occurred while creating timesheet", ex);
            throw ex;
        }
    }
}
