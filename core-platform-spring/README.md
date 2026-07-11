# Core Platform — Spring Boot

Cổng chuyển đổi (port) của dự án **.NET 8 `Core.Platform`** (Timesheet Management API) sang **Spring Boot 3.3 / Java 21**, giữ nguyên kiến trúc **Clean Architecture + DDD + CQRS**.

> Tài liệu thiết kế chi tiết: [`docs/DESIGN.md`](docs/DESIGN.md)

## Yêu cầu môi trường

| Thành phần | Phiên bản |
|-----------|-----------|
| JDK       | 21+       |
| Maven     | 3.9+ (hoặc dùng Maven tích hợp trong IDE) |
| MySQL     | 8.x, database `TimeSheet` (tái sử dụng schema do EF Core tạo) |

> Máy hiện tại **chưa cài JDK/Maven** nên project chưa được biên dịch thử. Cài JDK 21 + Maven (hoặc mở bằng IntelliJ/VS Code Java) rồi build như bên dưới.

## Cấu hình

Sửa `src/main/resources/application.yml` cho khớp MySQL của bạn (mặc định lấy đúng connection string của bản .NET):

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/TimeSheet?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: Thiendia@95
app:
  api-key: ""     # để trống = tắt cổng API key (giống mặc định .NET)
```

`spring.jpa.hibernate.ddl-auto` để `none` — ứng dụng **không** đụng vào DDL, dùng lại các bảng sẵn có (`ClassRooms`, `SalaryRooms`, `StudentClasses`, ...).

## Build & Run

```bash
cd d:/SourceCode/core-platform-spring

# Build + chạy test
mvn clean verify

# Chạy ứng dụng
mvn spring-boot:run

# Hoặc đóng gói rồi chạy jar
mvn clean package
java -jar target/core-platform-1.0.0.jar
```

Ứng dụng chạy ở `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## API (giữ nguyên route của bản .NET, base `/api/TimeSheets`)

| Method | Path | Mô tả |
|--------|------|-------|
| POST   | `/api/TimeSheets`             | Tạo timesheet |
| GET    | `/api/TimeSheets?month&year&page&pageSize` | Danh sách timesheet gom nhóm theo tháng |
| PUT    | `/api/TimeSheets/{id}`        | Cập nhật timesheet |
| DELETE | `/api/TimeSheets/{id}`        | Xoá timesheet (hard delete) |
| POST   | `/api/TimeSheets/Students`    | Tạo học sinh |
| GET    | `/api/TimeSheets/Students?page&pageSize&search` | Danh sách học sinh |
| PUT    | `/api/TimeSheets/Students/{id}` | Cập nhật học sinh |
| DELETE | `/api/TimeSheets/Students/{id}` | Xoá học sinh (soft delete) |
| POST   | `/api/TimeSheets/Classrooms`  | Tạo lớp |
| GET    | `/api/TimeSheets/Classrooms?page&pageSize&search` | Danh sách lớp |
| GET    | `/api/TimeSheets/Classrooms/{id}` | Chi tiết lớp |
| PUT    | `/api/TimeSheets/Classrooms/{id}` | Cập nhật lớp |
| DELETE | `/api/TimeSheets/Classrooms/{id}` | Xoá lớp (soft delete) |
| POST   | `/api/TimeSheets/Salary`      | Tạo mức lương cơ sở |

> **Lưu ý:** Spring so khớp route **phân biệt hoa/thường** (khác ASP.NET Core). Frontend phải gọi đúng casing `/api/TimeSheets/...` như route template gốc.

## Cấu trúc thư mục (rút gọn)

```
src/main/java/vn/aequitas/coreplatform/
├── CorePlatformApplication.java        # entry point (≈ Program.cs)
├── domain/                             # Entities, Enums, ports (Repository/UnitOfWork), utils
├── application/
│   ├── common/                         # mediator, bus, validation, exception, dto
│   └── timesheet/                      # command/query/handler, dto, service, validator, helper
├── infrastructure/                     # mediator impl, command/query runner, JPA repo + UnitOfWork
└── web/                                # controller, filter (API key), advice (error handler)
```

Xem chi tiết ánh xạ .NET → Spring trong [`docs/DESIGN.md`](docs/DESIGN.md).
