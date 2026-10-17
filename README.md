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
   (`users`, `students`, `courses`, `enrollments`) and a sample account `admin / admin123`.

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

## Run the project

```bash
mvn spring-boot:run
```

Open http://localhost:8080 to check that it works.

## Project structure

```
src/main/java/java03/group4/      Java code (controller, entity, repository, service)
src/main/resources/templates/     Thymeleaf views
src/main/resources/static/        CSS, JS
docs/database.sql                 database creation script
```

## Team members and contributions

| Member | Contribution |
|--------|--------------|
|        |              |
