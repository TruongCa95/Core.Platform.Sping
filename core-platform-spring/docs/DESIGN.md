# Tài liệu thiết kế — Core Platform (Spring Boot)

> Bản port của **.NET 8 `Core.Platform`** (Timesheet Management API) sang **Spring Boot 3.3 / Java 21**, theo **Clean Architecture + Domain-Driven Design**, tách **Command/Query (CQRS-style)** bằng **service thuần Spring** (idiomatic Spring Boot).

---

## 1. Mục tiêu & phạm vi

- Chuyển đổi **1:1 về hành vi** hệ thống quản lý bảng công (timesheet) từ .NET sang Spring Boot: cùng nghiệp vụ, cùng route, cùng hình dạng JSON, cùng schema database.
- **Dùng đúng "cách Spring Boot production hay dùng"**: controller gọi thẳng `@Service`, validation bằng **Jakarta Bean Validation**, truy cập dữ liệu bằng **Spring Data JPA**, transaction bằng `@Transactional`. Không port nguyên xi các abstraction .NET (MediatR, FluentValidation, Repository/UnitOfWork tự viết).
- **Tương thích dữ liệu**: dùng lại đúng các bảng MySQL do EF Core migration tạo (`ClassRooms`, `SalaryRooms`, `StudentClasses`, ...), GUID lưu dạng `char(36)`.

Những gì **không** đổi so với bản .NET: business rules (tính lương, hệ số Ki, phụ cấp VLB, thuế từ thiện, gom nhóm theo tháng), quy tắc validation, mã trạng thái HTTP, tên route, hình dạng JSON.

> **Lịch sử:** bản đầu tiên port nguyên các pattern .NET (mediator tự viết, FluentValidation clone, `Repository`/`UnitOfWork`). Sau đó được refactor sang cách Spring Boot idiomatic — tài liệu này mô tả trạng thái **sau** refactor.

---

## 2. Nguyên tắc kiến trúc

### 2.1 Clean Architecture — quy tắc phụ thuộc

Phụ thuộc chỉ hướng **vào trong**. Tầng ngoài biết tầng trong, không có chiều ngược lại.

```
        ┌─────────────────────────────────────────────┐
        │                    web                        │   Controller, Filter, @RestControllerAdvice
        │   ┌───────────────────────────────────────┐   │
        │   │             application                │   │   @Service (Command/Query), DTO, Bean Validation
        │   │   ┌───────────────────────────────┐   │   │
        │   │   │            domain              │   │   │   Entity, Enum, Spring Data Repository (port), util
        │   │   └───────────────────────────────┘   │   │
        │   └───────────────────────────────────────┘   │
        └─────────────────────────────────────────────┘
```

- **domain**: trái tim nghiệp vụ — entity, enum, và các **repository interface** (Spring Data). Chỉ dùng annotation JPA/Hibernate cho mapping + Spring Data cho repository port.
- **application**: use-cases dưới dạng **service** (tách Command/Query), DTO request/result. Phụ thuộc domain.
- **web**: HTTP adapter (controller, filter, advice). Phụ thuộc application.

> **Không còn tầng `infrastructure` riêng**: trước đây tầng này chứa hiện thực mediator/runner và `JpaRepositoryImpl`/`JpaUnitOfWork`. Với Spring Data JPA, **impl của repository được Spring sinh proxy lúc runtime** từ interface trong `domain.repository`, nên không cần adapter viết tay. Nếu sau này có adapter hạ tầng thật (message queue, external client...) thì mới dựng lại tầng này.

### 2.2 DDD

- **Entity / Aggregate**: `TimeSheet`, `ClassRoom`, `Students`, `Salary` là các aggregate; `ClassRoomTimeSheet`, `StudentClasses`, `TimesheetReview` là các entity liên kết/chi tiết. Tất cả kế thừa `BaseEntity` (Id + audit + soft-delete).
- **Một repository Spring Data cho mỗi aggregate/entity**, gom trong một transaction bằng `@Transactional` ở tầng service.
- **Ubiquitous language** giữ nguyên tên miền nghiệp vụ từ bản .NET (Timesheet, Classroom, Salary, Ki, Level...).

### 2.3 CQRS-style bằng service thuần

- **Command** (ghi) và **Query** (đọc) tách nhau ở mức **service riêng theo aggregate**: `XxxCommandService` (create/update/delete) và `XxxQueryService` (get/list).
- Không dùng mediator/dispatcher. Controller inject thẳng đúng service cần dùng và gọi method có tên nghiệp vụ rõ ràng (`classroomCommand.create(...)`, `classroomQuery.getById(...)`).
- Cross-cutting concern được xử lý theo cơ chế sẵn có của Spring: **validation** bằng `@Valid` (trước khi vào service), **transaction/log** bằng `@Transactional`/SLF4J, lỗi bằng `@RestControllerAdvice`.

---

## 3. Ánh xạ công nghệ .NET → Spring

| Vai trò | .NET 8 | Spring Boot |
|--------|--------|-------------|
| Nền tảng web | ASP.NET Core Web API | Spring Web MVC |
| DI container | `Microsoft.Extensions.DependencyInjection` | Spring IoC |
| Điều phối use-case | MediatR (`IMediator.Send`) + `ICommandRunner`/`IQueryRunner` | **Controller gọi thẳng `@Service`** (Command/Query service theo aggregate) |
| ORM | EF Core 8 + Pomelo MySQL | Spring Data JPA + Hibernate 6 + MySQL Connector/J |
| Repository / UoW | `IRepository<T>`, `IUnitOfWork` | **Spring Data** `JpaRepository<T,UUID>` + `JpaSpecificationExecutor<T>`; UoW = `@Transactional` |
| Truy vấn động | `Expression<Func<T,bool>>` (`IQueryable`) | `Specification<T>` (Spring Data JPA) |
| Validation | FluentValidation (`AbstractValidator`, `RuleFor`) | **Jakarta Bean Validation** (`@NotBlank/@Size/@Positive/@NotNull/@DecimalMin`...) + `@Valid` |
| Validation pipeline | `ValidationBehavior<TRequest,TResponse>` | Tự động: `@Valid` ở `@RequestBody` → `MethodArgumentNotValidException` |
| Xử lý lỗi tập trung | `ErrorHandlingMiddleware` | `@RestControllerAdvice GlobalExceptionHandler` |
| API key | `ApiKeyMiddleware` | `ApiKeyFilter extends OncePerRequestFilter` |
| OpenAPI/Swagger | Swashbuckle | springdoc-openapi |
| Cấu hình | `appsettings.json` | `application.yml` |
| Serialize JSON | System.Text.Json (web defaults: camelCase) + `JsonStringEnumConverter` | Jackson (mặc định camelCase) + enum theo tên |
| Log | `ILogger<T>` | SLF4J |
| Test | xUnit | JUnit 5 |
| Giảm boilerplate | (auto-properties) | Lombok |

---

## 4. Cấu trúc package

```
vn.aequitas.coreplatform
├── CorePlatformApplication                 # @SpringBootApplication (≈ Program.cs)
│
├── domain                                  # ==== TẦNG DOMAIN ====
│   ├── common/BaseEntity                    # Id (char36) + IsActive + audit; implements Persistable<UUID>
│   ├── enums/                               # KiEnums, LevelEnums, LocationEnums, ClassRoomStatusEnums
│   │        + LevelEnumsConverter, ClassRoomStatusEnumsConverter, EnumJson
│   ├── entity/timesheet/                    # ClassRoom, TimeSheet, Students, Salary,
│   │                                        # ClassRoomTimeSheet, StudentClasses, TimesheetReview
│   ├── repository/                          # Spring Data repository (1 interface / entity)
│   │                                        # ClassRoomRepository, StudentsRepository, TimeSheetRepository,
│   │                                        # SalaryRepository, ClassRoomTimeSheetRepository,
│   │                                        # StudentClassesRepository, TimesheetReviewRepository
│   └── util/CurrencyConverter
│
├── application                             # ==== TẦNG APPLICATION ====
│   ├── common
│   │   ├── exception/                       # DuplicateNameException, NotFoundException
│   │   └── dto/PagedResult<T>
│   └── timesheet
│       ├── command/<usecase>/               # <Command> — request body (create/update × timesheet/student/classroom + salary)
│       │                                    #   có annotation Bean Validation, KHÔNG còn handler
│       ├── query/<usecase>/                 # <Result> DTO (GetClassroomQueryResult, ...) — KHÔNG còn query/handler
│       ├── dto/                             # ClassroomDTO, TimeSheetDTO, TimesheetReviewDTO, CalculationSalaryRequest/Response
│       ├── service/                         # ⭐ nơi chứa nghiệp vụ:
│       │                                    #   ClassroomCommandService / ClassroomQueryService
│       │                                    #   StudentCommandService  / StudentQueryService
│       │                                    #   TimesheetCommandService / TimesheetQueryService
│       │                                    #   SalaryCommandService
│       │                                    #   CalculationSalaryService (+Impl)
│       └── helper/TimeHelper
│
└── web                                     # ==== TẦNG WEB ====
    ├── controller/TimeSheetsController       # inject thẳng các service; @Valid @RequestBody
    ├── filter/ApiKeyFilter
    └── advice/GlobalExceptionHandler, ErrorResponse
```

> Các folder `command/<usecase>` giờ **chỉ còn class request** (body của create/update) với annotation validation; folder `query/<usecase>` **chỉ còn class Result**. Toàn bộ `*Handler`, mediator, bus, và các wrapper `Delete*Command`/`Get*Query` đã bị gỡ — logic dồn vào các service.

---

## 5. Domain model

### 5.1 Sơ đồ quan hệ

```
Students ──< StudentClasses >── ClassRoom ──< ClassRoomTimeSheet >── TimeSheet
   │              (studentId,          │            (classRoomId,          │
   │               classId)            │             timeSheetId,          │
   │                                   │             numberOfStudent)      │
   └──────────< TimesheetReview >──────┴───────────────────────────────────┘
                (studentId, timesheetId, review, progress)

Salary  (level, numberOfStudent, money)   — bảng tra cứu lương cơ sở, độc lập
```

Hai quan hệ many-to-many được **hiện thực bằng entity join tường minh** (`StudentClasses`, `ClassRoomTimeSheet`) — giống bản .NET. Các navigation collection (`ClassRoom.TimeSheets`, `Students.ClassRooms`) **bị loại bỏ có chủ đích** vì toàn bộ service thao tác trực tiếp qua repository của entity join và tự join thủ công; navigation không hề được đọc/ghi ở bất kỳ đâu → bỏ đi để tránh `LazyInitializationException` mà vẫn đúng hành vi.

### 5.2 `BaseEntity`

| Field | Kiểu | Cột DB | Ghi chú |
|-------|------|--------|--------|
| id | UUID | `Id char(36)` | gán ở tầng ứng dụng (`UUID.randomUUID()`), không dùng `@GeneratedValue` |
| isActive | boolean | `IsActive tinyint(1)` | cờ soft-delete |
| createdBy / updatedBy | String | `longtext` | audit (mặc định `""`) |
| createdDate / updatedDate | LocalDateTime | `datetime(6)` | mặc định `now(UTC)` — khớp `DateTime.UtcNow` |

**`BaseEntity implements Persistable<UUID>`.** Vì khóa chính do ứng dụng gán (luôn khác `null`), Spring Data **không** suy được `save()` là INSERT hay UPDATE, và mặc định sẽ chạy `merge` (kèm một `SELECT` dò tồn tại) cho mọi `save`. Để giữ đúng ngữ nghĩa **INSERT thẳng** của repository .NET cũ, `BaseEntity` khai báo cờ `@Transient newEntity = true` cùng `isNew()`; callback `@PostLoad`/`@PostPersist` set cờ về `false` sau khi row được load/persist. Nhờ đó entity mới → `persist` (INSERT), entity đã load → `merge` (UPDATE).

### 5.3 Enums

| Enum | Giá trị (giữ nguyên số) | Persist? |
|------|--------------------------|----------|
| `LevelEnums` | PrimarySchool=1, SecondarySchool=2, HighSchool=3, **Other=99** | Có → cần converter (giá trị ≠ ordinal) |
| `ClassRoomStatusEnums` | Active=0, Paused=1, Inactive=2 | Có → converter |
| `KiEnums` | APlus=0, A=1, B=2, BPlus=3, C=4, D=5 | Không (chỉ ở DTO) |
| `LocationEnums` | None=0, VLB=1, TC=2 | Không (chỉ ở DTO) |

Vì `LevelEnums.Other = 99` (không trùng ordinal), **bắt buộc** dùng `AttributeConverter` để lưu đúng số như EF Core, thay vì `@Enumerated(ORDINAL)`. `LevelEnumsConverter`/`ClassRoomStatusEnumsConverter` khai báo `@Converter(autoApply = true)` nên tự áp cho mọi field cùng kiểu.

Về JSON: mỗi enum có `@JsonCreator` (qua helper `EnumJson`) nhận **cả tên hằng (không phân biệt hoa/thường) lẫn giá trị số**, còn serialize ra vẫn là **tên hằng** — khớp đúng `JsonStringEnumConverter` của .NET.

---

## 6. Ánh xạ Database

`ddl-auto: none` → ứng dụng **không tạo/sửa schema**, chỉ đọc-ghi trên bảng có sẵn.

| Entity | Bảng | Ghi chú tên bảng |
|--------|------|------------------|
| ClassRoom | `ClassRooms` | |
| TimeSheet | `TimeSheets` | |
| Students | `Students` | giữ tên class số nhiều để map 1:1 |
| Salary | `SalaryRooms` | **tên bảng "lạ"** kế thừa từ model .NET |
| ClassRoomTimeSheet | `ClassRoomTimeSheets` | FK nullable |
| StudentClasses | `StudentClasses` | |
| TimesheetReview | `TimesheetReviews` | `progress decimal(18,2)` nullable |

Quy ước cột được **chỉ định tường minh** để khớp PascalCase mà EF Core sinh ra (Hibernate mặc định đổi sang snake_case nên phải `@Column(name = "...")`):

- GUID: `@JdbcTypeCode(SqlTypes.CHAR) @Column(columnDefinition = "char(36)")` → Hibernate đọc/ghi UUID ở dạng chuỗi 36 ký tự (lowercase, có gạch) đúng như Pomelo/EF.
- Tiền: `Money decimal(18,4)`, `Progress decimal(18,2)` → `BigDecimal`.
- Ngày: `datetime(6)` → `LocalDateTime`.
- Chuỗi: `longtext`.
- `boolean` → `tinyint(1)`.

> Muốn kiểm tra schema lúc khởi động, đổi `ddl-auto: validate` (chặt hơn, có thể báo lệch kiểu nhỏ). Mặc định để `none` cho an toàn với DB hiện có.

---

## 7. Truy cập dữ liệu — Spring Data JPA

### 7.1 Repository (`domain.repository`)

Mỗi entity có **một interface** kế thừa Spring Data:

```java
public interface ClassRoomRepository
        extends JpaRepository<ClassRoom, UUID>, JpaSpecificationExecutor<ClassRoom> {
}
```

- `JpaRepository` cấp sẵn `save/saveAll/findById/findAll/delete/deleteAll/flush...`.
- `JpaSpecificationExecutor` cấp `findOne(Specification)`, `findAll(Specification[, Sort|Pageable])`, `count(Specification)` — thay cho `Expression<Func<T,bool>>` của EF. Các truy vấn động (lọc active, tìm kiếm LIKE nhiều cột, phân trang có sort) viết bằng `Specification` ngay trong service.

Spring sinh proxy hiện thực lúc runtime; **không có class impl viết tay**.

### 7.2 Ghi & khóa gán sẵn

Nhờ `BaseEntity implements Persistable` (mục 5.2), `repo.save(entity)` với entity mới chạy **INSERT** (không SELECT-then-merge). Các thao tác batch dùng `saveAll(...)`, xóa dùng `delete(entity)`/`deleteAll(entities)`.

### 7.3 Ranh giới transaction (Unit of Work)

Mỗi method **command service** đánh dấu `@Transactional`, mỗi method **query service** `@Transactional(readOnly = true)`. Đây chính là "unit of work": mọi `save/delete` trong một method service cùng nằm trong một transaction, commit một lần khi method kết thúc — thay cho `IUnitOfWork` của .NET.

Với các thao tác **nhạy cảm thứ tự** (xóa con trước khi xóa cha; xóa rồi chèn lại join rows), `TimesheetCommandService` gọi `flush()` tường minh để ép đúng thứ tự thao tác DB, tránh vi phạm khóa ngoại từ schema EF Core.

---

## 8. Tầng service (nghiệp vụ)

Toàn bộ nghiệp vụ nằm ở `application.timesheet.service`, tách Command/Query theo aggregate:

| Service | Method | Nguồn (handler .NET/cũ) |
|---------|--------|--------------------------|
| `ClassroomCommandService` | `create` / `update` / `delete` | Create/Update/DeleteClassroom...Handler |
| `ClassroomQueryService` | `getById` / `getList` | GetClassroom / GetListClassroom Handler |
| `StudentCommandService` | `create` / `update` / `delete` | Create/Update/DeleteStudent...Handler |
| `StudentQueryService` | `getList` | GetListStudentHandler |
| `TimesheetCommandService` | `create` / `update` / `delete` | Create/Update/DeleteTimesheet...Handler |
| `TimesheetQueryService` | `getList` | GetListTimesheetHandler |
| `SalaryCommandService` | `createBaseSalary` | CreateBaseSalaryHandler |
| `CalculationSalaryService` (+Impl) | `calculationSalary` | ICalculationSalaryService |

### Luồng một request

```
HTTP  ──►  TimeSheetsController
              │  @Valid @RequestBody  (Bean Validation chạy trước khi vào method)
              │  (với update: gán id từ path vào command)
              ▼
       XxxCommandService / XxxQueryService   (@Transactional)   ← nghiệp vụ
              │  gọi Spring Data repository (Specification khi cần)
              ▼
       Hibernate  ──►  MySQL
```

Controller phân biệt rõ đọc/ghi bằng cách inject **đúng** command-service hay query-service — thể hiện ý định giống `ICommandRunner`/`IQueryRunner` cũ nhưng không cần lớp trung gian.

---

## 9. Validation — Jakarta Bean Validation

Bản .NET dùng FluentValidation + `ValidationBehavior`. Bản này dùng **Bean Validation chuẩn** (`spring-boot-starter-validation`, Hibernate Validator):

- **Annotation trên command DTO**: `@NotBlank`, `@Size(max=...)`, `@Positive`, `@PositiveOrZero`, `@NotNull`, `@DecimalMin(inclusive=false)`...
- **`@Valid @RequestBody`** ở controller: Spring validate trước khi gọi service; lỗi → `MethodArgumentNotValidException` (xử lý ở mục 10, trả 400).

Ánh xạ rule cũ → mới:

| FluentValidation (cũ) | Bean Validation (mới) |
|-----------------------|-----------------------|
| `NotEmpty()` (chuỗi) | `@NotBlank` |
| `MaximumLength(n)` | `@Size(max = n)` |
| `GreaterThan(0)` (int) | `@Positive` |
| `GreaterThanOrEqualTo(0)` | `@PositiveOrZero` |
| `GreaterThan(0)` (BigDecimal) | `@NotNull` + `@DecimalMin(value="0", inclusive=false)` |
| `IsInEnum()` | `@NotNull` (enum đã lệ thuộc Jackson: giá trị sai bị chặn khi bind) |
| `NotNull()` (list) | `@NotNull` |

Ví dụ đối chiếu:

```java
// Java — Bean Validation
@NotBlank(message = "Class code is required.")
@Size(max = 50, message = "Class code cannot exceed 50 characters.")
private String classCode = "";
```
```csharp
// C# — FluentValidation
RuleFor(x => x.ClassCode)
    .NotEmpty().WithMessage("Class code is required.")
    .MaximumLength(50).WithMessage("Class code cannot exceed 50 characters.");
```

> **`id` khi update** lấy từ path (`@PathVariable`) nên **không** gắn ràng buộc trên field `id` của command (vì `@Valid` chạy lúc bind, trước khi controller gán id từ path). Controller vẫn giữ logic "body id rỗng/all-zero → lấy id từ path".

---

## 10. Xử lý lỗi

`GlobalExceptionHandler` (`@RestControllerAdvice`) ánh xạ exception → HTTP giống `ErrorHandlingMiddleware`, trả body `{ "error": ..., "statusCode": ... }`:

| Exception | HTTP |
|-----------|------|
| `DuplicateNameException` | 409 Conflict |
| `MethodArgumentNotValidException` (Bean Validation) | 400 Bad Request |
| `IllegalArgumentException` | 400 Bad Request |
| `NotFoundException` | 404 Not Found |
| còn lại | 500 Internal Server Error |

Với lỗi validation, handler gom các `FieldError` thành message dạng ` -- <field>: <message>` (nhiều dòng) — tương tự cách FluentValidation nối message trước đây.

---

## 11. Bảo mật — API key

`ApiKeyFilter` (`OncePerRequestFilter`) tái hiện `ApiKeyMiddleware`: **opt-in** — chỉ chặn khi cấu hình `app.api-key` khác rỗng; luôn cho qua `/swagger*` và `/v3/api-docs*`; thiếu/sai header `X-Api-Key` → 401 JSON. Để trống = tắt (thuận tiện dev). Đây là chỗ để sau này thay bằng JWT/OAuth.

---

## 12. Hợp đồng JSON

- **Casing**: ASP.NET Core web-defaults dùng **camelCase**; Jackson mặc định cũng camelCase → khớp. Bật thêm `accept-case-insensitive-properties` + `accept-case-insensitive-enums` để nhận input linh hoạt như ASP.NET.
- **Enum**: serialize theo **tên hằng** (`"PrimarySchool"`, `"APlus"`), nhận cả tên lẫn số.
- **Ngày**: ISO-8601 (`2026-07-11T00:00:00`). Field `date` là `LocalDateTime` nên input nên có phần giờ.
- **Số thập phân**: `BigDecimal` serialize thành số (giống `decimal`).

---

## 13. Business logic (giữ nguyên)

### 13.1 Tính lương (`CalculationSalaryService`)

Tra `SalaryRooms` theo `level`; nếu `numberOfStudent >= 99` (ngưỡng max) thì lấy bậc lương cao nhất của level đó, ngược lại lấy đúng bậc theo số học sinh; nhân **hệ số Ki**:

| Ki | Hệ số |
|----|-------|
| APlus | 1.4 |
| A | 1.25 |
| BPlus | 1.1 |
| B / khác | 1.0 |
| C | 0.8 |
| D | 0.5 |

### 13.2 Danh sách timesheet (`TimesheetQueryService.getList`)

Method phức tạp nhất. Bản .NET dùng LINQ join dịch sang SQL rồi materialize; bản Java **load các bảng (nhỏ) rồi join trong bộ nhớ** bằng stream, giữ nguyên quy tắc:

1. Join `ClassRoomTimeSheet` → `TimeSheet` → `ClassRoom` (inner), group-join `TimesheetReview` theo `timesheetId` (review được nhân bản cho mỗi dòng lớp của cùng timesheet), tên học sinh lấy từ `Students`.
2. Lọc theo `month` (`"MMM"`, ví dụ `Jul`, không phân biệt hoa/thường) và/hoặc `year` — chỉ lọc khi có ít nhất một tham số.
3. Tính lương mỗi dòng (mục 13.1, nhưng theo `numberOfStudent` của dòng, không nhân Ki), **phụ cấp**: `classCode` bắt đầu `VLB` → `50000`, còn lại `0`.
4. Sắp xếp giảm dần theo ngày, gom nhóm theo `"MMM yyyy"` (giữ thứ tự mới nhất trước). Mỗi nhóm: `allowanceTotal`, `grossTotal = Σ totalSalary`, `taxforCharity = Σ salary × 2/100`, `netTotal = grossTotal − taxforCharity`.
5. Phân trang theo **số nhóm tháng**.

### 13.3 Quy tắc xoá

- Classroom, Student: **soft delete** (`isActive = false`) và soft-delete các bản ghi enrolment liên quan.
- Timesheet: **hard delete** cả bản ghi + `ClassRoomTimeSheet` + `TimesheetReview` liên quan (xóa con trước, `flush()`, rồi xóa cha).

---

## 14. Cấu hình

| Khoá | Ý nghĩa |
|------|---------|
| `spring.datasource.*` | Kết nối MySQL (mặc định = connection string bản .NET) |
| `spring.jpa.hibernate.ddl-auto` | `none` (không đụng DDL) |
| `app.api-key` | Khoá API tùy chọn; rỗng = tắt |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` |

Profile `dev` (`application-dev.yml`) bật `show-sql`. Kích hoạt: `--spring.profiles.active=dev`.

---

## 15. Build / Run / Test

Xem [`README.md`](../README.md). Tóm tắt: cần **JDK 21 + Maven**, `mvn clean verify` để build+test, `mvn spring-boot:run` để chạy. (Máy hiện tại chưa cài JDK/Maven nên chưa biên dịch thử — build lại trong IDE.)

Test: `CalculationSalaryServiceImplTest` — port của `CalculationSalaryServiceTests` (kiểm hệ số Ki bằng JUnit 5 parameterized; `calculateKi` thuần nên khởi tạo service với repository `null`).

---

## 16. Khác biệt & lưu ý (caveats)

1. **Route phân biệt hoa/thường**: Spring MVC so khớp path có phân biệt hoa/thường, khác ASP.NET Core. Route giữ đúng template gốc (`/api/TimeSheets/...`); frontend phải gọi đúng casing.
2. **Join trong bộ nhớ** ở `TimesheetQueryService.getList`: kết quả giống hệt bản SQL-join, nhưng tải toàn bảng liên quan — phù hợp quy mô ứng dụng timesheet, cần xem lại nếu dữ liệu lớn.
3. **`TimesheetReviewDTO.name`**: bản .NET gắn `[JsonIgnore]` của **Newtonsoft** trong khi API serialize bằng **System.Text.Json** (bỏ qua annotation đó) → thực tế `name` **vẫn** xuất hiện. Bản port giữ đúng hành vi quan sát được (serialize `name`).
4. **Khóa gán sẵn**: `BaseEntity implements Persistable` để `save()` chạy INSERT thẳng (xem 5.2); tính nguyên tử do `@Transactional` ở service đảm bảo.
5. **Thứ tự flush trong timesheet**: `update`/`delete` gọi `flush()` tường minh để xóa-trước-chèn / xóa-con-trước-cha, giữ đúng hành vi & tránh vi phạm FK.
6. **`DeleteTimesheet` không lọc `IsActive`** khi tìm timesheet — giữ đúng bản gốc.

---

## 17. Hướng mở rộng

- Thêm use-case mới = thêm method vào service tương ứng (hoặc service mới cho aggregate mới) + DTO request (kèm annotation Bean Validation) + endpoint controller.
- Cross-cutting concern (logging, caching, audit) = dùng Spring AOP (`@Aspect`) hoặc decorator quanh service, thay cho `PipelineBehavior` trước đây.
- Validation phức tạp (kiểm tra chéo DB) = viết `ConstraintValidator` tùy biến, hoặc kiểm tra như business rule trong service (ví dụ chống trùng tên → `DuplicateNameException`).
- Thay API key bằng Spring Security (JWT/OAuth2) tại tầng `web`.
- Nếu cần enforce ranh giới compile-time giữa các tầng, có thể tách thành multi-module Maven (hiện dùng single-module + package theo tầng).
