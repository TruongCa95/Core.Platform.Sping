# Tài liệu thiết kế — Core Platform (Spring Boot)

> Bản port của **.NET 8 `Core.Platform`** (Timesheet Management API) sang **Spring Boot 3.3 / Java 21**, giữ nguyên **Clean Architecture + Domain-Driven Design + CQRS**.

---

## 1. Mục tiêu & phạm vi

- Chuyển đổi **1:1 về hành vi** hệ thống quản lý bảng công (timesheet) từ .NET sang Spring Boot: cùng nghiệp vụ, cùng route, cùng hình dạng JSON, cùng schema database.
- **Giữ nguyên kiến trúc**: 4 tầng Clean Architecture, tách Command/Query (CQRS), pipeline validation, Repository + Unit of Work, middleware xử lý lỗi & API key.
- **Tương thích dữ liệu**: dùng lại đúng các bảng MySQL do EF Core migration tạo (`ClassRooms`, `SalaryRooms`, `StudentClasses`, ...), GUID lưu dạng `char(36)`.

Những gì **không** đổi so với bản .NET: business rules (tính lương, hệ số Ki, phụ cấp VLB, thuế từ thiện, gom nhóm theo tháng), quy tắc validation, mã trạng thái HTTP, tên route.

---

## 2. Nguyên tắc kiến trúc

### 2.1 Clean Architecture — quy tắc phụ thuộc

Phụ thuộc chỉ hướng **vào trong**. Tầng ngoài biết tầng trong, không có chiều ngược lại.

```
        ┌─────────────────────────────────────────────┐
        │                    web                        │   Controller, Filter, @RestControllerAdvice
        │   ┌───────────────────────────────────────┐   │
        │   │             infrastructure             │   │   Mediator impl, Runner, JPA Repo, UnitOfWork
        │   │   ┌───────────────────────────────┐   │   │
        │   │   │          application           │   │   │   Command/Query + Handler, DTO, Service,
        │   │   │   ┌───────────────────────┐   │   │   │   Validator, Mediator/Validation abstractions
        │   │   │   │        domain          │   │   │   │   Entity, Enum, Repository/UnitOfWork (port), util
        │   │   │   └───────────────────────┘   │   │   │
        │   │   └───────────────────────────────┘   │   │
        │   └───────────────────────────────────────┘   │
        └─────────────────────────────────────────────┘
```

- **domain**: trái tim nghiệp vụ — entity, enum, và các **port** (`Repository`, `UnitOfWork`). Không phụ thuộc Spring (chỉ dùng annotation JPA/Hibernate cho mapping — tương tự bản .NET domain phụ thuộc `System.Linq.Expressions`).
- **application**: use-cases dưới dạng CQRS handler + các abstraction dùng chung (mediator, validation). Phụ thuộc domain.
- **infrastructure**: hiện thực các port bằng Spring Data JPA + hiện thực mediator/runner. Phụ thuộc application + domain.
- **web**: HTTP adapter. Phụ thuộc application (gọi qua `CommandRunner`/`QueryRunner`).

### 2.2 DDD

- **Entity / Aggregate**: `TimeSheet`, `ClassRoom`, `Students`, `Salary` là các aggregate; `ClassRoomTimeSheet`, `StudentClasses`, `TimesheetReview` là các entity liên kết/chi tiết. Tất cả kế thừa `BaseEntity` (Id + audit + soft-delete).
- **Repository per aggregate** truy cập qua **Unit of Work** để gom trong một transaction.
- **Ubiquitous language** giữ nguyên tên miền nghiệp vụ từ bản .NET (Timesheet, Classroom, Salary, Ki, Level...).

### 2.3 CQRS

- **Command** (ghi) và **Query** (đọc) là các đối tượng `Request<R>` riêng biệt, mỗi cái có đúng **một** `RequestHandler`.
- Điều phối qua **Mediator** (thay cho MediatR), bọc bởi **pipeline behavior** (validation).

---

## 3. Ánh xạ công nghệ .NET → Spring

| Vai trò | .NET 8 | Spring Boot |
|--------|--------|-------------|
| Nền tảng web | ASP.NET Core Web API | Spring Web MVC |
| DI container | `Microsoft.Extensions.DependencyInjection` | Spring IoC |
| CQRS mediator | MediatR (`IRequest`, `IRequestHandler`, `IPipelineBehavior`, `IMediator`) | Mediator tự viết (`Request`, `RequestHandler`, `PipelineBehavior`, `Mediator`) |
| Command/Query facade | `ICommandRunner` / `IQueryRunner` | `CommandRunner` / `QueryRunner` |
| ORM | EF Core 8 + Pomelo MySQL | Spring Data JPA + Hibernate 6 + MySQL Connector/J |
| Repository/UoW | `IRepository<T>`, `IUnitOfWork` | `Repository<T>`, `UnitOfWork` (port) + `JpaRepositoryImpl`, `JpaUnitOfWork` |
| Truy vấn động | `Expression<Func<T,bool>>` (`IQueryable`) | `Specification<T>` (Spring Data JPA) |
| Validation | FluentValidation (`AbstractValidator`, `RuleFor`) | Framework fluent tự viết (`AbstractValidator`, `ruleFor`) |
| Validation pipeline | `ValidationBehavior<TRequest,TResponse>` | `ValidationBehavior implements PipelineBehavior` |
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
│   ├── common/BaseEntity                    # Id (char36) + IsActive + audit
│   ├── enums/                               # KiEnums, LevelEnums, LocationEnums, ClassRoomStatusEnums
│   │        + LevelEnumsConverter, ClassRoomStatusEnumsConverter, EnumJson
│   ├── entity/timesheet/                    # ClassRoom, TimeSheet, Students, Salary,
│   │                                        # ClassRoomTimeSheet, StudentClasses, TimesheetReview
│   ├── repository/                          # Repository<T>, UnitOfWork  (PORTS)
│   └── util/CurrencyConverter
│
├── application                             # ==== TẦNG APPLICATION ====
│   ├── common
│   │   ├── mediator/                        # Request, RequestHandler, PipelineBehavior, Mediator
│   │   ├── bus/                             # CommandRunner, QueryRunner (interface)
│   │   ├── validation/                      # Validator, AbstractValidator, RuleBuilder, Rule, Rules,
│   │   │                                    # ValidatorRegistry, ValidationBehavior, ValidationException, ValidationFailure
│   │   ├── exception/                       # DuplicateNameException, NotFoundException
│   │   └── dto/PagedResult<T>
│   └── timesheet
│       ├── command/<usecase>/               # <Command> + <Handler>   (create/update/delete × timesheet/student/classroom, + salary)
│       ├── query/<usecase>/                 # <Query> + <Handler> + <Result>
│       ├── dto/                             # ClassroomDTO, TimeSheetDTO, TimesheetReviewDTO, CalculationSalaryRequest/Response
│       ├── service/                         # CalculationSalaryService (+Impl)
│       ├── validator/                       # 7 validator
│       └── helper/TimeHelper
│
├── infrastructure                          # ==== TẦNG INFRASTRUCTURE ====
│   ├── mediator/DefaultMediator             # đăng ký handler theo request-type + chạy pipeline
│   ├── bus/                                 # DefaultCommandRunner, DefaultQueryRunner
│   └── persistence/                         # JpaRepositoryImpl<T>, JpaUnitOfWork
│
└── web                                     # ==== TẦNG WEB ====
    ├── controller/TimeSheetsController
    ├── filter/ApiKeyFilter
    └── advice/GlobalExceptionHandler, ErrorResponse
```

Mỗi package `command/query` con tương ứng 1 folder trong `TimeSheetManagement` của bản .NET để dễ đối chiếu.

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

Hai quan hệ many-to-many được **hiện thực bằng entity join tường minh** (`StudentClasses`, `ClassRoomTimeSheet`) — giống bản .NET. Các navigation collection (`ClassRoom.TimeSheets`, `Students.ClassRooms`) **bị loại bỏ có chủ đích** vì toàn bộ handler thao tác trực tiếp qua repository của entity join và tự join thủ công; navigation không hề được đọc/ghi ở bất kỳ đâu → bỏ đi để tránh `LazyInitializationException` mà vẫn đúng hành vi.

### 5.2 `BaseEntity`

| Field | Kiểu | Cột DB | Ghi chú |
|-------|------|--------|--------|
| id | UUID | `Id char(36)` | gán ở tầng ứng dụng (`UUID.randomUUID()`), không dùng `@GeneratedValue` |
| isActive | boolean | `IsActive tinyint(1)` | cờ soft-delete |
| createdBy / updatedBy | String | `longtext` | audit (mặc định `""`) |
| createdDate / updatedDate | LocalDateTime | `datetime(6)` | mặc định `now(UTC)` — khớp `DateTime.UtcNow` |

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

## 7. Repository & Unit of Work

### 7.1 Port (`domain.repository`)

`Repository<T>` là bản dịch của `IRepository<T>`; thay `Expression<Func<T,bool>>` bằng `Specification<T>`:

```
List<T> getAll();
T getById(UUID id);                    // ném NotFoundException nếu không có (≈ GetByIdAsync)
Optional<T> getOne(Specification<T>);  // ≈ GetOne
List<T> getListByCondition(Specification<T> [, Sort]);
long count(Specification<T>);
List<T> getPaged(Specification<T>, Sort, page, pageSize);
List<T> getListByIds(Collection<UUID>);
void add / addRange / update / updateRange / deleteById / deleteByIds;
```

`UnitOfWork` expose 7 repository theo entity + `complete()`:

```
Repository<TimeSheet> timeSheets();  Repository<ClassRoom> classrooms();  ... ;  int complete();
```

### 7.2 Hiện thực (`infrastructure.persistence`)

- **`JpaRepositoryImpl<T>`**: bọc `SimpleJpaRepository<T,UUID>` (dựng từ `EntityManager`) cho phần đọc + `Specification`. Phần ghi dùng `EntityManager.persist/merge/remove` rồi **`flush()` ngay** — tái hiện đúng kiểu `SaveChangesAsync()` gọi sau mỗi thao tác của repository .NET. Vì Id do ứng dụng gán nên `persist` = INSERT (không SELECT trước như `save`).
- **`JpaUnitOfWork`**: `@PersistenceContext EntityManager` (proxy bám theo transaction hiện tại), tạo lazy một `JpaRepositoryImpl` cho mỗi entity và cache lại; `complete()` = `flush()`.

### 7.3 Ranh giới transaction

Mỗi **command handler** đánh dấu `@Transactional`, mỗi **query handler** `@Transactional(readOnly = true)`. Đây chính là "unit of work" thật: nhiều lệnh `add/update/delete` (mỗi lệnh flush) cùng nằm trong một transaction, commit một lần khi handler kết thúc — thay cho việc `IUnitOfWork` scoped của .NET.

---

## 8. CQRS Mediator

### 8.1 Abstraction (`application.common.mediator`)

| Type | Vai trò | Tương đương MediatR |
|------|---------|---------------------|
| `Request<R>` | marker cho command/query trả về `R` | `IRequest<TResponse>` |
| `RequestHandler<C extends Request<R>, R>` | xử lý đúng một loại request | `IRequestHandler<TRequest,TResponse>` |
| `PipelineBehavior` | bước cross-cutting bọc quanh handler (dùng generic method để 1 behavior áp cho mọi request) | `IPipelineBehavior<,>` |
| `Mediator` | định tuyến request → handler qua pipeline | `IMediator` |

### 8.2 `DefaultMediator`

Lúc khởi động, inject `List<RequestHandler<?,?>>` và `List<PipelineBehavior>`:

1. Với mỗi handler, dùng `AopUtils.getTargetClass` (bỏ proxy `@Transactional`) + `ResolvableType...as(RequestHandler.class).getGeneric(0)` để lấy **loại request cụ thể**, rồi map `requestClass → handler`. Trùng handler cho cùng request → ném lỗi.
2. `send(request)`: tra handler theo `request.getClass()`, dựng chuỗi `Supplier<R>` bọc lần lượt bởi các behavior (theo thứ tự đảo), rồi thực thi.

### 8.3 Luồng một request

```
HTTP  ──►  TimeSheetsController
              │  build Command/Query object
              ▼
       CommandRunner / QueryRunner            (log, ≈ ICommandRunner/IQueryRunner)
              │  mediator.send(request)
              ▼
       DefaultMediator                        (tra handler theo request-type)
              │
              ▼
       PipelineBehavior chain
         └─ ValidationBehavior                (chạy validator; lỗi → ValidationException 400)
              │  next.get()
              ▼
       RequestHandler  (@Transactional)       (nghiệp vụ)
              │  UnitOfWork → Repository → JPA/Hibernate
              ▼
       MySQL
```

`CommandRunner` và `QueryRunner` là **hai interface tách biệt** (dù cùng gọi mediator) để controller thể hiện rõ ý định đọc/ghi — đúng như bản .NET.

---

## 9. Validation framework

Bản .NET dùng FluentValidation + `ValidationBehavior`. Ở đây tái tạo một mini-framework fluent để các validator **đọc gần như y hệt** bản C#.

- **`AbstractValidator<T>`**: khai báo rule trong constructor bằng `ruleFor("PropertyName", T::getter)`. Tự resolve `Class<T>` qua reflection để đăng ký theo request-type.
- **`RuleBuilder`**: các method dây chuyền `notEmpty()`, `notNull()`, `maximumLength(n)`, `greaterThan(x)`, `greaterThanOrEqualTo(x)`, `isInEnum()`, và `withMessage(...)` (ghi đè message của rule ngay trước — đúng ngữ nghĩa FluentValidation).
- **`Rules`**: predicate dùng chung, khớp built-in FluentValidation (ví dụ `NotEmpty` từ chối null/chuỗi rỗng/collection rỗng/UUID toàn 0).
- **`ValidatorRegistry`**: gom mọi `Validator<?>` bean, index theo request-type (≈ MediatR resolve `IEnumerable<IValidator<T>>`).
- **`ValidationBehavior`**: trước khi handler chạy, lấy validator theo `request.getClass()`, gom `ValidationFailure`, nếu có thì ném `ValidationException` → 400.

Ví dụ đối chiếu:

```java
// Java
ruleFor("ClassCode", CreateClassroomCommand::getClassCode)
    .notEmpty().withMessage("Class code is required.")
    .maximumLength(50).withMessage("Class code cannot exceed 50 characters.");
```
```csharp
// C#
RuleFor(x => x.ClassCode)
    .NotEmpty().WithMessage("Class code is required.")
    .MaximumLength(50).WithMessage("Class code cannot exceed 50 characters.");
```

> `CreateBaseSalaryCommandHandler` giữ nguyên việc **validate thủ công trong handler** (ngoài pipeline) đúng như bản gốc.

---

## 10. Xử lý lỗi

`GlobalExceptionHandler` (`@RestControllerAdvice`) ánh xạ exception → HTTP giống `ErrorHandlingMiddleware`, trả body `{ "error": ..., "statusCode": ... }`:

| Exception | HTTP |
|-----------|------|
| `DuplicateNameException` | 409 Conflict |
| `ValidationException` | 400 Bad Request |
| `IllegalArgumentException` | 400 Bad Request |
| `NotFoundException` | 404 Not Found |
| còn lại | 500 Internal Server Error |

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

### 13.2 Danh sách timesheet (`GetListTimesheetQueryHandler`)

Handler phức tạp nhất. Bản .NET dùng LINQ join dịch sang SQL rồi materialize; bản Java **load các bảng (nhỏ) rồi join trong bộ nhớ** bằng stream, giữ nguyên quy tắc:

1. Join `ClassRoomTimeSheet` → `TimeSheet` → `ClassRoom` (inner), group-join `TimesheetReview` theo `timesheetId` (review được nhân bản cho mỗi dòng lớp của cùng timesheet), tên học sinh lấy từ `Students`.
2. Lọc theo `month` (`"MMM"`, ví dụ `Jul`, không phân biệt hoa/thường) và/hoặc `year` — chỉ lọc khi có ít nhất một tham số.
3. Tính lương mỗi dòng (mục 13.1, nhưng theo `numberOfStudent` của dòng, không nhân Ki), **phụ cấp**: `classCode` bắt đầu `VLB` → `50000`, còn lại `0`.
4. Sắp xếp giảm dần theo ngày, gom nhóm theo `"MMM yyyy"` (giữ thứ tự mới nhất trước). Mỗi nhóm: `allowanceTotal`, `grossTotal = Σ totalSalary`, `taxforCharity = Σ salary × 2/100`, `netTotal = grossTotal − taxforCharity`.
5. Phân trang theo **số nhóm tháng**.

### 13.3 Quy tắc xoá

- Classroom, Student: **soft delete** (`isActive = false`) và soft-delete các bản ghi enrolment liên quan.
- Timesheet: **hard delete** cả bản ghi + `ClassRoomTimeSheet` + `TimesheetReview` liên quan.

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

Xem [`README.md`](../README.md). Tóm tắt: cần **JDK 21 + Maven**, `mvn clean verify` để build+test, `mvn spring-boot:run` để chạy. (Máy hiện tại chưa cài JDK/Maven nên chưa biên dịch thử.)

Test: `CalculationSalaryServiceImplTest` — port của `CalculationSalaryServiceTests` (kiểm hệ số Ki bằng JUnit 5 parameterized).

---

## 16. Khác biệt & lưu ý (caveats)

1. **Route phân biệt hoa/thường**: Spring MVC so khớp path có phân biệt hoa/thường, khác ASP.NET Core. Route giữ đúng template gốc (`/api/TimeSheets/...`); frontend phải gọi đúng casing.
2. **Join trong bộ nhớ** ở `GetListTimesheet`: kết quả giống hệt bản SQL-join, nhưng tải toàn bảng liên quan — phù hợp quy mô ứng dụng timesheet, cần xem lại nếu dữ liệu lớn.
3. **`TimesheetReviewDTO.name`**: bản .NET gắn `[JsonIgnore]` của **Newtonsoft** trong khi API serialize bằng **System.Text.Json** (bỏ qua annotation đó) → thực tế `name` **vẫn** xuất hiện. Bản port giữ đúng hành vi quan sát được (serialize `name`).
4. **Flush theo từng thao tác**: repository flush sau mỗi `add/update/delete` để mô phỏng `SaveChangesAsync` của .NET; tính nguyên tử do `@Transactional` trên handler đảm bảo.
5. **`complete()` trả `0`**: các handler không dùng giá trị trả về (khác `SaveChanges` trả số dòng); giữ chữ ký cho khớp `IUnitOfWork.CompleteAsync`.
6. **`DeleteTimesheet` không lọc `IsActive`** khi tìm timesheet — giữ đúng bản gốc.

---

## 17. Hướng mở rộng

- Thêm behavior mới (logging, transaction, caching) = thêm một `PipelineBehavior` bean.
- Thêm use-case mới = thêm `Request` + `RequestHandler` (+ `Validator` nếu cần); mediator tự đăng ký, không sửa chỗ khác.
- Thay API key bằng Spring Security (JWT/OAuth2) tại tầng `web`.
- Nếu cần enforce ranh giới compile-time giữa các tầng, có thể tách thành multi-module Maven (hiện dùng single-module + package theo tầng).
```
