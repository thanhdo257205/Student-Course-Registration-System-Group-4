# Student Course Registration System

Team project (Option 4): manage students, courses and course enrollments.
Spring Boot 3 + Thymeleaf + SQL Server. Maven group `java03`, artifact `group4`.

## Team workflow

**You must create your own branch to push.** Do not push directly to `main`.

```bash
git checkout -b your-branch-name
git add .
git commit -m "describe your change"
git push -u origin your-branch-name
```

## Requirements

- JDK 17+
- Maven (or the Maven bundled with IntelliJ)
- SQL Server (with SSMS or Azure Data Studio), TCP/IP enabled on port 1433

## Database setup

1. Open `docs/database.sql` in SSMS / Azure Data Studio.
2. Run the whole script. It creates the `student_course_db` database with 4 tables
   (`users`, `students`, `courses`, `enrollments`) and sample accounts for an administrator,
   school staff member, and student. Student logins are linked to a student record.

> **Warning:** Running `docs/database.sql` drops and recreates `student_course_db` if it
> already exists. All existing data in that database will be permanently deleted.

### Connection configuration

`src/main/resources/application.properties` reads its settings from environment variables.
If they are not set, the defaults below are used (localhost, user `sa`, password `changeme`).

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | JDBC URL | `jdbc:sqlserver://localhost:1433;databaseName=student_course_db;encrypt=true;trustServerCertificate=true` |
| `DB_USERNAME` | SQL Server login | `sa` |
| `DB_PASSWORD` | Password | `changeme` |

Set the variables on your machine. Never write your real password into a file you commit.

```bash
# Windows PowerShell
$env:DB_PASSWORD="your_password"
# macOS / Linux
export DB_PASSWORD="your_password"
```

In IntelliJ: Run > Edit Configurations > Environment variables.

On startup, `src/main/resources/schema.sql` safely adds the nullable `users.student_id`
column and its relationship/index when upgrading an existing database. It does not
drop tables or data. Hibernate continues to validate the resulting schema.

## Run the project

```bash
mvn spring-boot:run
```

Open http://localhost:8080 to check that it works.

The app requires a login. Use one of the accounts created by `docs/database.sql`:

| Role | Username | Password | Access |
|------|----------|----------|--------|
| Administrator | `admin` | `admin123` | Manage student, course, and enrollment records |
| School staff | `schoolstaff` | `staff123` | Manage student, course, and enrollment records |
| Student (Nguyễn Văn Minh) | `minh.nguyen` | `minh123` | Browse and register for courses |

Run `docs/mockData.sql` after the database setup to add 30 courses, 500 students and up to
500 enrollments, plus 49 linked student accounts named after the student's email
(for example `an.nguyen001`, `an.tran002`), all with password `password123`.

## Project structure

```
src/main/java/java03/group4/      Java code, one package per module (modular monolith):
  auth/                           login, session, role check (User, AuthService, interceptor)
  student/                        student records (entity, repository, form, controller)
  course/                         course records
  enrollment/                     staff enrollment records + student catalog/registration
  shared/                         helpers used by several modules (paging)
src/main/resources/templates/     Thymeleaf views
src/main/resources/static/        CSS, JS
docs/database.sql                 database creation script
```

## Team members and contributions

| Member | Contribution |
|--------|--------------|
|        |              |
