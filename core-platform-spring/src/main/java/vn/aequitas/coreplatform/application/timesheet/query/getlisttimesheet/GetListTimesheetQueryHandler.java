package vn.aequitas.coreplatform.application.timesheet.query.getlisttimesheet;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.aequitas.coreplatform.application.common.mediator.RequestHandler;
import vn.aequitas.coreplatform.application.timesheet.dto.TimeSheetDTO;
import vn.aequitas.coreplatform.application.timesheet.dto.TimesheetReviewDTO;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoom;
import vn.aequitas.coreplatform.domain.entity.timesheet.ClassRoomTimeSheet;
import vn.aequitas.coreplatform.domain.entity.timesheet.Salary;
import vn.aequitas.coreplatform.domain.entity.timesheet.Students;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimeSheet;
import vn.aequitas.coreplatform.domain.entity.timesheet.TimesheetReview;
import vn.aequitas.coreplatform.domain.enums.LevelEnums;
import vn.aequitas.coreplatform.domain.repository.UnitOfWork;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Port of the .NET {@code GetListTimesheetQueryHandler}.
 *
 * <p>The original composed the result with EF/LINQ joins translated to SQL and then
 * materialized; here the same joins are performed in memory after loading the
 * (small) tables, preserving the exact business rules: base salary lookup by
 * level/student-count with a max tier, a 50,000 VLB allowance, a 2% charity tax,
 * and month/year grouping ordered most-recent-first.</p>
 */
@Component
public class GetListTimesheetQueryHandler implements RequestHandler<GetListTimesheetQuery, PagedTimesheetResult> {

    private static final DateTimeFormatter MONTH_ABBR =
            DateTimeFormatter.ofPattern("MMM", java.util.Locale.ENGLISH);
    private static final DateTimeFormatter MONTH_YEAR =
            DateTimeFormatter.ofPattern("MMM yyyy", java.util.Locale.ENGLISH);
    private static final BigDecimal VLB_ALLOWANCE = new BigDecimal("50000");
    private static final BigDecimal TWO = new BigDecimal("2");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final UnitOfWork unitOfWork;

    public GetListTimesheetQueryHandler(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    /** Flattened classroom-timesheet row, before salary computation. */
    private record Row(UUID timesheetId, UUID classroomId, String description, LocalDateTime date,
                       int numberOfStudent, LevelEnums level, String classCode, List<TimesheetReviewDTO> reviews) {
    }

    @Override
    @Transactional(readOnly = true)
    public PagedTimesheetResult handle(GetListTimesheetQuery request) {
        // --- Load the tables and index them for the in-memory joins ---
        List<ClassRoomTimeSheet> links = unitOfWork.classRoomTimeSheets()
                .getListByCondition((root, query, cb) -> cb.isNotNull(root.get("timeSheetId")));

        Map<UUID, TimeSheet> timesheetById = unitOfWork.timeSheets().getAll().stream()
                .collect(Collectors.toMap(TimeSheet::getId, Function.identity(), (a, b) -> a));
        Map<UUID, ClassRoom> classroomById = unitOfWork.classrooms().getAll().stream()
                .collect(Collectors.toMap(ClassRoom::getId, Function.identity(), (a, b) -> a));
        Map<UUID, String> studentNameById = unitOfWork.students().getAll().stream()
                .collect(Collectors.toMap(Students::getId, Students::getName, (a, b) -> a));
        Map<UUID, List<TimesheetReview>> reviewsByTimesheet = unitOfWork.timesheetReviews().getAll().stream()
                .collect(Collectors.groupingBy(TimesheetReview::getTimesheetId));

        // --- Inner-join links -> timesheet -> classroom, group-join the reviews ---
        List<Row> rows = links.stream()
                .filter(link -> timesheetById.containsKey(link.getTimeSheetId())
                        && classroomById.containsKey(link.getClassRoomId()))
                .map(link -> {
                    TimeSheet ts = timesheetById.get(link.getTimeSheetId());
                    ClassRoom cls = classroomById.get(link.getClassRoomId());
                    List<TimesheetReviewDTO> reviews = reviewsByTimesheet
                            .getOrDefault(ts.getId(), List.of()).stream()
                            .map(r -> {
                                TimesheetReviewDTO dto = new TimesheetReviewDTO();
                                dto.setStudentId(r.getStudentId());
                                dto.setName(studentNameById.getOrDefault(r.getStudentId(), ""));
                                dto.setReview(r.getReview());
                                dto.setProgress(r.getProgress());
                                return dto;
                            })
                            .collect(Collectors.toList());
                    return new Row(ts.getId(), cls.getId(), ts.getDescription(), ts.getDate(),
                            link.getNumberOfStudent(), cls.getLevel(), cls.getClassCode(), reviews);
                })
                .collect(Collectors.toList());

        List<Salary> salaries = unitOfWork.salaries()
                .getListByCondition((root, query, cb) -> cb.equal(root.get("isActive"), true));

        // --- Optional month/year filter (only when at least one is supplied) ---
        String reqMonth = request.getMonth();
        Integer reqYear = request.getYear();
        if (StringUtils.hasText(reqMonth) || reqYear != null) {
            rows = rows.stream().filter(row -> {
                boolean monthMatch = true;
                boolean yearMatch = true;
                if (StringUtils.hasText(reqMonth)) {
                    monthMatch = row.date().format(MONTH_ABBR).equalsIgnoreCase(reqMonth);
                }
                if (reqYear != null) {
                    yearMatch = row.date().getYear() == reqYear;
                }
                return monthMatch && yearMatch;
            }).collect(Collectors.toList());
        }

        // --- Highest student-count tier available per level ---
        Map<LevelEnums, Integer> maxStudentByLevel = salaries.stream()
                .collect(Collectors.groupingBy(Salary::getLevel,
                        Collectors.mapping(Salary::getNumberOfStudent,
                                Collectors.reducing(0, Integer::max))));

        // --- Compute salary/allowance per row, ordered by date descending ---
        List<TimeSheetDTO> computed = rows.stream()
                .map(row -> {
                    List<Salary> salaryList = salaries.stream()
                            .filter(s -> s.getLevel() == row.level())
                            .toList();
                    BigDecimal amount = BigDecimal.ZERO;
                    if (!salaryList.isEmpty()) {
                        int maxStudent = maxStudentByLevel.get(row.level());
                        int lookup = row.numberOfStudent() >= maxStudent ? maxStudent : row.numberOfStudent();
                        amount = salaryList.stream()
                                .filter(s -> s.getNumberOfStudent() == lookup)
                                .map(Salary::getMoney)
                                .findFirst()
                                .orElse(BigDecimal.ZERO);
                    }

                    BigDecimal allowance = row.classCode() != null && row.classCode().startsWith("VLB")
                            ? VLB_ALLOWANCE : BigDecimal.ZERO;

                    TimeSheetDTO dto = new TimeSheetDTO();
                    dto.setId(row.timesheetId());
                    dto.setClassroomId(row.classroomId());
                    dto.setDescription(row.description());
                    dto.setClasscode(row.classCode());
                    dto.setDate(row.date());
                    dto.setNumberOfStudent(row.numberOfStudent());
                    dto.setLevel(row.level());
                    dto.setSalary(amount);
                    dto.setAllowance(allowance);
                    dto.setTotalSalary(amount.add(allowance));
                    dto.setReviews(row.reviews());
                    return dto;
                })
                .sorted(Comparator.comparing(TimeSheetDTO::getDate).reversed())
                .toList();

        // --- Group by "MMM yyyy" preserving most-recent-first order ---
        Map<String, List<TimeSheetDTO>> grouped = new LinkedHashMap<>();
        for (TimeSheetDTO dto : computed) {
            String key = dto.getDate().format(MONTH_YEAR);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(dto);
        }

        List<TimesheetResult> groupedResult = new ArrayList<>();
        for (Map.Entry<String, List<TimeSheetDTO>> entry : grouped.entrySet()) {
            List<TimeSheetDTO> group = entry.getValue();
            BigDecimal allowanceTotal = sum(group, TimeSheetDTO::getAllowance);
            BigDecimal grossTotal = sum(group, TimeSheetDTO::getTotalSalary);
            BigDecimal salaryTotal = sum(group, TimeSheetDTO::getSalary);
            BigDecimal taxForCharity = salaryTotal.multiply(TWO).divide(HUNDRED);
            groupedResult.add(TimesheetResult.builder()
                    .month(entry.getKey())
                    .timeSheet(group)
                    .allowanceTotal(allowanceTotal)
                    .grossTotal(grossTotal)
                    .taxforCharity(taxForCharity)
                    .netTotal(grossTotal.subtract(taxForCharity))
                    .build());
        }

        // --- Paginate the month groups ---
        int page = request.getPage() <= 0 ? 1 : request.getPage();
        int pageSize = request.getPageSize() <= 0 ? 20 : request.getPageSize();
        int totalCount = groupedResult.size();
        List<TimesheetResult> pageItems = groupedResult.stream()
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .toList();

        return PagedTimesheetResult.builder()
                .results(pageItems)
                .page(page)
                .pageSize(pageSize)
                .totalCount(totalCount)
                .totalPages((int) Math.ceil(totalCount / (double) pageSize))
                .build();
    }

    private static BigDecimal sum(List<TimeSheetDTO> items, Function<TimeSheetDTO, BigDecimal> selector) {
        return items.stream()
                .map(selector)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
