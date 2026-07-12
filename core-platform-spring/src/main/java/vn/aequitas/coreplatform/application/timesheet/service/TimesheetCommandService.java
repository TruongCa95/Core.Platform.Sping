package vn.aequitas.coreplatform.application.timesheet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.aequitas.coreplatform.application.timesheet.command.createtimesheet.CreateTimesheetCommand;
import vn.aequitas.coreplatform.application.timesheet.command.updatetimesheet.UpdateTimesheetCommand;
import vn.aequitas.coreplatform.application.timesheet.helper.TimeHelper;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoomTimeSheet;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimeSheet;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimesheetReview;
import vn.aequitas.coreplatform.domain.repository.ClassRoomTimeSheetRepository;
import vn.aequitas.coreplatform.domain.repository.TimeSheetRepository;
import vn.aequitas.coreplatform.domain.repository.TimesheetReviewRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Write-side operations for timesheets. Consolidates the former
 * {@code CreateTimeSheetCommandHandler}, {@code UpdateTimesheetCommandHandler} and
 * {@code DeleteTimesheetByIdCommandHandler}.
 */
@Service
public class TimesheetCommandService {

    private static final Logger log = LoggerFactory.getLogger(TimesheetCommandService.class);

    private final TimeSheetRepository timeSheets;
    private final ClassRoomTimeSheetRepository classRoomTimeSheets;
    private final TimesheetReviewRepository timesheetReviews;

    public TimesheetCommandService(TimeSheetRepository timeSheets,
                                   ClassRoomTimeSheetRepository classRoomTimeSheets,
                                   TimesheetReviewRepository timesheetReviews) {
        this.timeSheets = timeSheets;
        this.classRoomTimeSheets = classRoomTimeSheets;
        this.timesheetReviews = timesheetReviews;
    }

    @Transactional
    public UUID create(CreateTimesheetCommand request) {
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

            // Persist the timesheet before its dependent rows so the foreign keys resolve.
            timeSheets.save(timesheet);
            classRoomTimeSheets.saveAll(relationships);
            timesheetReviews.saveAll(reviews);
            return timesheet.getId();
        } catch (Exception ex) {
            log.error("Error occurred while creating timesheet", ex);
            throw ex;
        }
    }

    @Transactional
    public boolean update(UpdateTimesheetCommand request) {
        Optional<TimeSheet> found = timeSheets.findOne((root, query, cb) -> cb.and(
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
        timeSheets.save(timesheet);

        // Replace the classroom links when the caller supplies them.
        if (request.getClassrooms() != null && !request.getClassrooms().isEmpty()) {
            List<ClassRoomTimeSheet> existing = classRoomTimeSheets.findAll(
                    (root, query, cb) -> cb.equal(root.get("timeSheetId"), request.getId()));
            if (!existing.isEmpty()) {
                classRoomTimeSheets.deleteAll(existing);
                classRoomTimeSheets.flush(); // remove old rows before inserting the replacements
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
            classRoomTimeSheets.saveAll(relationships);
        }

        // Replace the reviews with the set from the form (add / edit / remove).
        List<TimesheetReview> existingReviews = timesheetReviews.findAll(
                (root, query, cb) -> cb.equal(root.get("timesheetId"), request.getId()));
        if (!existingReviews.isEmpty()) {
            timesheetReviews.deleteAll(existingReviews);
            timesheetReviews.flush(); // remove old rows before inserting the replacements
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
            timesheetReviews.saveAll(reviews);
        }
        return true;
    }

    @Transactional
    public boolean delete(UUID id) {
        Optional<TimeSheet> found = timeSheets.findOne((root, query, cb) -> cb.equal(root.get("id"), id));
        if (found.isEmpty()) {
            return false;
        }
        TimeSheet timesheet = found.get();

        List<ClassRoomTimeSheet> relationships = classRoomTimeSheets.findAll(
                (root, query, cb) -> cb.equal(root.get("timeSheetId"), id));
        if (!relationships.isEmpty()) {
            classRoomTimeSheets.deleteAll(relationships);
        }

        List<TimesheetReview> reviews = timesheetReviews.findAll(
                (root, query, cb) -> cb.equal(root.get("timesheetId"), id));
        if (!reviews.isEmpty()) {
            timesheetReviews.deleteAll(reviews);
        }

        // Flush the child deletes before removing the parent so foreign keys stay satisfied.
        timeSheets.flush();
        timeSheets.delete(timesheet);
        return true;
    }
}
