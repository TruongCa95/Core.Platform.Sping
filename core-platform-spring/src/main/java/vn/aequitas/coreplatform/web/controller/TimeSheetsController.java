package vn.aequitas.coreplatform.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.aequitas.coreplatform.application.common.dto.PagedResult;
import vn.aequitas.coreplatform.application.timesheet.command.createbasesalary.CreateBaseSalaryCommand;
import vn.aequitas.coreplatform.application.timesheet.command.createclassroom.CreateClassroomCommand;
import vn.aequitas.coreplatform.application.timesheet.command.createstudent.CreateStudentCommand;
import vn.aequitas.coreplatform.application.timesheet.command.createtimesheet.CreateTimesheetCommand;
import vn.aequitas.coreplatform.application.timesheet.command.updateclassroom.UpdateClassroomCommand;
import vn.aequitas.coreplatform.application.timesheet.command.updatestudent.UpdateStudentCommand;
import vn.aequitas.coreplatform.application.timesheet.command.updatetimesheet.UpdateTimesheetCommand;
import vn.aequitas.coreplatform.application.timesheet.query.getclassroombyid.GetClassroomQueryResult;
import vn.aequitas.coreplatform.application.timesheet.query.getlistclassroom.GetListClassroomQueryResult;
import vn.aequitas.coreplatform.application.timesheet.query.getliststudent.GetListStudentQueryResult;
import vn.aequitas.coreplatform.application.timesheet.query.getlisttimesheet.PagedTimesheetResult;
import vn.aequitas.coreplatform.application.timesheet.service.ClassroomCommandService;
import vn.aequitas.coreplatform.application.timesheet.service.ClassroomQueryService;
import vn.aequitas.coreplatform.application.timesheet.service.SalaryCommandService;
import vn.aequitas.coreplatform.application.timesheet.service.StudentCommandService;
import vn.aequitas.coreplatform.application.timesheet.service.StudentQueryService;
import vn.aequitas.coreplatform.application.timesheet.service.TimesheetCommandService;
import vn.aequitas.coreplatform.application.timesheet.service.TimesheetQueryService;

import java.util.UUID;

/**
 * REST endpoints for timesheets, students, classrooms and base salary. Direct
 * port of the .NET {@code TimeSheetsController}; routes and status-code semantics
 * are preserved (base path {@code /api/TimeSheets}). Each endpoint delegates to the
 * matching application service; request bodies are validated with {@code @Valid}.
 */
@RestController
@RequestMapping("/api/TimeSheets")
public class TimeSheetsController {

    private static final UUID EMPTY_UUID = new UUID(0L, 0L);

    private final TimesheetCommandService timesheetCommand;
    private final TimesheetQueryService timesheetQuery;
    private final StudentCommandService studentCommand;
    private final StudentQueryService studentQuery;
    private final ClassroomCommandService classroomCommand;
    private final ClassroomQueryService classroomQuery;
    private final SalaryCommandService salaryCommand;

    public TimeSheetsController(TimesheetCommandService timesheetCommand,
                                TimesheetQueryService timesheetQuery,
                                StudentCommandService studentCommand,
                                StudentQueryService studentQuery,
                                ClassroomCommandService classroomCommand,
                                ClassroomQueryService classroomQuery,
                                SalaryCommandService salaryCommand) {
        this.timesheetCommand = timesheetCommand;
        this.timesheetQuery = timesheetQuery;
        this.studentCommand = studentCommand;
        this.studentQuery = studentQuery;
        this.classroomCommand = classroomCommand;
        this.classroomQuery = classroomQuery;
        this.salaryCommand = salaryCommand;
    }

    // ----- Timesheets -----

    @PostMapping
    public ResponseEntity<UUID> createTimeSheet(@Valid @RequestBody CreateTimesheetCommand command) {
        return ResponseEntity.ok(timesheetCommand.create(command));
    }

    @GetMapping
    public ResponseEntity<PagedTimesheetResult> getTimeSheets(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PagedTimesheetResult result = timesheetQuery.getList(month, year, page, pageSize);
        if (result == null || result.getResults().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateTimeSheet(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateTimesheetCommand command) {
        if (command.getId() == null || command.getId().equals(EMPTY_UUID)) {
            command.setId(id);
        }
        boolean result = timesheetCommand.update(command);
        return result ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteTimeSheet(@PathVariable UUID id) {
        boolean result = timesheetCommand.delete(id);
        return result ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    // ----- Students -----

    @PostMapping("/Students")
    public ResponseEntity<UUID> createStudent(@Valid @RequestBody CreateStudentCommand command) {
        return ResponseEntity.ok(studentCommand.create(command));
    }

    @GetMapping("/Students")
    public ResponseEntity<PagedResult<GetListStudentQueryResult>> getStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search) {
        PagedResult<GetListStudentQueryResult> result = studentQuery.getList(page, pageSize, search);
        if (result == null || result.getItems().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/Students/{id}")
    public ResponseEntity<Boolean> updateStudent(@PathVariable UUID id,
                                                 @Valid @RequestBody UpdateStudentCommand command) {
        if (command.getId() == null || command.getId().equals(EMPTY_UUID)) {
            command.setId(id);
        }
        boolean result = studentCommand.update(command);
        return result ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/Students/{id}")
    public ResponseEntity<Boolean> deleteStudent(@PathVariable UUID id) {
        boolean result = studentCommand.delete(id);
        return result ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    // ----- Classrooms -----

    @PostMapping("/Classrooms")
    public ResponseEntity<UUID> createClassroom(@Valid @RequestBody CreateClassroomCommand command) {
        return ResponseEntity.ok(classroomCommand.create(command));
    }

    @GetMapping("/Classrooms")
    public ResponseEntity<PagedResult<GetListClassroomQueryResult>> getClassrooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search) {
        PagedResult<GetListClassroomQueryResult> result = classroomQuery.getList(page, pageSize, search);
        if (result == null || result.getItems().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/Classrooms/{id}")
    public ResponseEntity<GetClassroomQueryResult> getClassroom(@PathVariable UUID id) {
        return ResponseEntity.ok(classroomQuery.getById(id));
    }

    @PutMapping("/Classrooms/{id}")
    public ResponseEntity<Boolean> updateClassroom(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateClassroomCommand command) {
        if (command.getId() == null || command.getId().equals(EMPTY_UUID)) {
            command.setId(id);
        }
        boolean result = classroomCommand.update(command);
        return result ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/Classrooms/{id}")
    public ResponseEntity<Boolean> deleteClassroom(@PathVariable UUID id) {
        boolean result = classroomCommand.delete(id);
        return result ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    // ----- Base salary -----

    @PostMapping("/Salary")
    public ResponseEntity<UUID> createSalary(@Valid @RequestBody CreateBaseSalaryCommand command) {
        return ResponseEntity.ok(salaryCommand.createBaseSalary(command));
    }
}
