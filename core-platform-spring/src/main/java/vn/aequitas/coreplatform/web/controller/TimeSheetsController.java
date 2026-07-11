package vn.aequitas.coreplatform.web.controller;

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
import vn.aequitas.coreplatform.application.common.bus.CommandRunner;
import vn.aequitas.coreplatform.application.common.bus.QueryRunner;
import vn.aequitas.coreplatform.application.common.dto.PagedResult;
import vn.aequitas.coreplatform.application.timesheet.command.createbasesalary.CreateBaseSalaryCommand;
import vn.aequitas.coreplatform.application.timesheet.command.createclassroom.CreateClassroomCommand;
import vn.aequitas.coreplatform.application.timesheet.command.createstudent.CreateStudentCommand;
import vn.aequitas.coreplatform.application.timesheet.command.createtimesheet.CreateTimesheetCommand;
import vn.aequitas.coreplatform.application.timesheet.command.deleteclassroom.DeleteClassroomByIdCommand;
import vn.aequitas.coreplatform.application.timesheet.command.deletestudent.DeleteStudentByIdCommand;
import vn.aequitas.coreplatform.application.timesheet.command.deletetimesheet.DeleteTimesheetByIdCommand;
import vn.aequitas.coreplatform.application.timesheet.command.updateclassroom.UpdateClassroomCommand;
import vn.aequitas.coreplatform.application.timesheet.command.updatestudent.UpdateStudentCommand;
import vn.aequitas.coreplatform.application.timesheet.command.updatetimesheet.UpdateTimesheetCommand;
import vn.aequitas.coreplatform.application.timesheet.query.getclassroombyid.GetClassroomQuery;
import vn.aequitas.coreplatform.application.timesheet.query.getclassroombyid.GetClassroomQueryResult;
import vn.aequitas.coreplatform.application.timesheet.query.getlistclassroom.GetListClassroomQuery;
import vn.aequitas.coreplatform.application.timesheet.query.getlistclassroom.GetListClassroomQueryResult;
import vn.aequitas.coreplatform.application.timesheet.query.getliststudent.GetListStudentQuery;
import vn.aequitas.coreplatform.application.timesheet.query.getliststudent.GetListStudentQueryResult;
import vn.aequitas.coreplatform.application.timesheet.query.getlisttimesheet.GetListTimesheetQuery;
import vn.aequitas.coreplatform.application.timesheet.query.getlisttimesheet.PagedTimesheetResult;

import java.util.UUID;

/**
 * REST endpoints for timesheets, students, classrooms and base salary. Direct
 * port of the .NET {@code TimeSheetsController}; routes and status-code semantics
 * are preserved (base path {@code /api/TimeSheets}).
 */
@RestController
@RequestMapping("/api/TimeSheets")
public class TimeSheetsController {

    private static final UUID EMPTY_UUID = new UUID(0L, 0L);

    private final CommandRunner command;
    private final QueryRunner query;

    public TimeSheetsController(CommandRunner command, QueryRunner query) {
        this.command = command;
        this.query = query;
    }

    // ----- Timesheets -----

    @PostMapping
    public ResponseEntity<UUID> createTimeSheet(@RequestBody CreateTimesheetCommand command) {
        return ResponseEntity.ok(this.command.send(command));
    }

    @GetMapping
    public ResponseEntity<PagedTimesheetResult> getTimeSheets(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        GetListTimesheetQuery request = new GetListTimesheetQuery();
        request.setMonth(month);
        request.setYear(year);
        request.setPage(page);
        request.setPageSize(pageSize);

        PagedTimesheetResult result = query.send(request);
        if (result == null || result.getResults().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateTimeSheet(@PathVariable UUID id,
                                                   @RequestBody UpdateTimesheetCommand command) {
        if (command.getId() == null || command.getId().equals(EMPTY_UUID)) {
            command.setId(id);
        }
        boolean result = this.command.send(command);
        return result ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteTimeSheet(@PathVariable UUID id) {
        DeleteTimesheetByIdCommand cmd = new DeleteTimesheetByIdCommand();
        cmd.setId(id);
        boolean result = command.send(cmd);
        return result ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    // ----- Students -----

    @PostMapping("/Students")
    public ResponseEntity<UUID> createStudent(@RequestBody CreateStudentCommand command) {
        return ResponseEntity.ok(this.command.send(command));
    }

    @GetMapping("/Students")
    public ResponseEntity<PagedResult<GetListStudentQueryResult>> getStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search) {
        GetListStudentQuery request = new GetListStudentQuery();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setSearch(search);

        PagedResult<GetListStudentQueryResult> result = query.send(request);
        if (result == null || result.getItems().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/Students/{id}")
    public ResponseEntity<Boolean> updateStudent(@PathVariable UUID id,
                                                 @RequestBody UpdateStudentCommand command) {
        if (command.getId() == null || command.getId().equals(EMPTY_UUID)) {
            command.setId(id);
        }
        boolean result = this.command.send(command);
        return result ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/Students/{id}")
    public ResponseEntity<Boolean> deleteStudent(@PathVariable UUID id) {
        DeleteStudentByIdCommand cmd = new DeleteStudentByIdCommand();
        cmd.setId(id);
        boolean result = command.send(cmd);
        return result ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    // ----- Classrooms -----

    @PostMapping("/Classrooms")
    public ResponseEntity<UUID> createClassroom(@RequestBody CreateClassroomCommand command) {
        return ResponseEntity.ok(this.command.send(command));
    }

    @GetMapping("/Classrooms")
    public ResponseEntity<PagedResult<GetListClassroomQueryResult>> getClassrooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search) {
        GetListClassroomQuery request = new GetListClassroomQuery();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setSearch(search);

        PagedResult<GetListClassroomQueryResult> result = query.send(request);
        if (result == null || result.getItems().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/Classrooms/{id}")
    public ResponseEntity<GetClassroomQueryResult> getClassroom(@PathVariable UUID id) {
        GetClassroomQuery request = new GetClassroomQuery();
        request.setClassroomId(id);
        return ResponseEntity.ok(query.send(request));
    }

    @PutMapping("/Classrooms/{id}")
    public ResponseEntity<Boolean> updateClassroom(@PathVariable UUID id,
                                                   @RequestBody UpdateClassroomCommand command) {
        if (command.getId() == null || command.getId().equals(EMPTY_UUID)) {
            command.setId(id);
        }
        boolean result = this.command.send(command);
        return result ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/Classrooms/{id}")
    public ResponseEntity<Boolean> deleteClassroom(@PathVariable UUID id) {
        DeleteClassroomByIdCommand cmd = new DeleteClassroomByIdCommand();
        cmd.setId(id);
        boolean result = command.send(cmd);
        return result ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    // ----- Base salary -----

    @PostMapping("/Salary")
    public ResponseEntity<UUID> createSalary(@RequestBody CreateBaseSalaryCommand command) {
        return ResponseEntity.ok(this.command.send(command));
    }
}
