-- Mock data for local testing. Run after docs/database.sql.
-- Safe to re-run: every insert skips rows that already exist.
-- In sqlcmd use the -I flag (QUOTED_IDENTIFIER ON); SSMS has it on by default.
USE student_course_db;
GO

SET NOCOUNT ON;
GO

-- 1. Courses: 30 courses with English titles.
DECLARE @courses TABLE (course_name NVARCHAR(100), instructor_name NVARCHAR(100), capacity INT);
INSERT INTO @courses VALUES
    (N'C# Programming Fundamentals',            N'Nguyễn Văn An',     40),
    (N'Advanced Database Systems',              N'Trần Thị Bình',     35),
    (N'Web Development with React',             N'Lê Hoàng Cường',    50),
    (N'Data Structures and Algorithms',         N'Phạm Minh Đức',     45),
    (N'Linux System Administration',               N'Hoàng Văn Em',      30),
    (N'Introduction to Artificial Intelligence', N'Vũ Thị Phương',    40),
    (N'Information Security',                   N'Đỗ Quốc Huy',       35),
    (N'Java Enterprise Development',            N'Nguyễn Thị Mai',    50),
    (N'UI/UX Design',                           N'Bùi Thanh Nga',     30),
    (N'Cloud Computing with AWS',               N'Phan Văn Phúc',     40),
    (N'Mobile App Development with Flutter',    N'Võ Thị Quỳnh',      45),
    (N'Software Testing',                       N'Ngô Văn Sơn',       35),
    (N'Big Data Analytics',                     N'Dương Thị Thùy',    40),
    (N'Agile Project Management with Scrum',    N'Lý Minh Tuấn',      30),
    (N'Python for Data Science',                N'Đinh Thị Uyên',     50),
    (N'Advanced Computer Networks',             N'Mai Văn Vinh',      35),
    (N'Microservices Architecture',             N'Tạ Thị Xuân',       40),
    (N'Blockchain Technology',                  N'Hồ Văn Yên',        30),
    (N'E-commerce Systems',                     N'Trương Thị Thanh',  45),
    (N'Object-Oriented Programming in C++',     N'Lương Văn Bảo',     40),
    (N'Backend Development with Node.js',       N'Cao Thị Châu',      50),
    (N'Game Development with Unity',            N'Đàm Văn Dũng',      35),
    (N'Discrete Mathematics',                   N'Hà Thị Giang',      45),
    (N'Introduction to Software Engineering',   N'Khuất Văn Hùng',    40),
    (N'Computer Vision and Image Processing',   N'Lại Thị Kiều',      30),
    (N'IoT and Embedded Programming',           N'Mạch Văn Long',     35),
    (N'Political Economy',                      N'Nghiêm Thị Mỹ',     60),
    (N'Introduction to Law',                    N'Phùng Văn Nam',     60),
    (N'Ho Chi Minh Ideology',                   N'Quách Thị Oanh',    60),
    (N'English for IT',                         N'Sầm Văn Phong',     50);

INSERT INTO courses (course_name, instructor_name, capacity)
SELECT seed.course_name, seed.instructor_name, seed.capacity
FROM @courses AS seed
WHERE NOT EXISTS (
    SELECT 1 FROM courses AS existing WHERE existing.course_name = seed.course_name
);
GO

-- 2. Students: 500 realistic names, no numbers in the name.
--    Each (family, given) pair is used once: 15 family x 34 given = 510 combinations.
--    Email: given.family + 3-digit number, e.g. an.nguyen001@student.edu.vn
DECLARE @family TABLE (idx INT PRIMARY KEY, name NVARCHAR(20), slug VARCHAR(20));
INSERT INTO @family VALUES
    (0, N'Nguyễn', 'nguyen'), (1, N'Trần', 'tran'),   (2, N'Lê', 'le'),       (3, N'Phạm', 'pham'),
    (4, N'Hoàng', 'hoang'),   (5, N'Huỳnh', 'huynh'), (6, N'Phan', 'phan'),   (7, N'Vũ', 'vu'),
    (8, N'Võ', 'vo'),         (9, N'Đặng', 'dang'),   (10, N'Bùi', 'bui'),    (11, N'Đỗ', 'do'),
    (12, N'Hồ', 'ho'),        (13, N'Ngô', 'ngo'),    (14, N'Dương', 'duong');

DECLARE @middle TABLE (idx INT PRIMARY KEY, name NVARCHAR(20));
INSERT INTO @middle VALUES
    (0, N'Văn'), (1, N'Thị'), (2, N'Minh'), (3, N'Gia'), (4, N'Thanh'),
    (5, N'Ngọc'), (6, N'Đức'), (7, N'Hoàng'), (8, N'Quốc'), (9, N'Thu');

DECLARE @given TABLE (idx INT PRIMARY KEY, name NVARCHAR(20), slug VARCHAR(20));
INSERT INTO @given VALUES
    (0, N'An', 'an'),       (1, N'Anh', 'anh'),     (2, N'Bảo', 'bao'),     (3, N'Bình', 'binh'),
    (4, N'Châu', 'chau'),   (5, N'Chi', 'chi'),     (6, N'Cường', 'cuong'), (7, N'Dũng', 'dung'),
    (8, N'Duy', 'duy'),     (9, N'Giang', 'giang'), (10, N'Hà', 'ha'),      (11, N'Hải', 'hai'),
    (12, N'Hạnh', 'hanh'),  (13, N'Hiếu', 'hieu'),  (14, N'Hoa', 'hoa'),    (15, N'Huy', 'huy'),
    (16, N'Hương', 'huong'), (17, N'Khánh', 'khanh'), (18, N'Khoa', 'khoa'), (19, N'Lan', 'lan'),
    (20, N'Linh', 'linh'),  (21, N'Long', 'long'),  (22, N'Mai', 'mai'),    (23, N'Minh', 'minh'),
    (24, N'My', 'my'),      (25, N'Nam', 'nam'),    (26, N'Ngọc', 'ngoc'),  (27, N'Nhung', 'nhung'),
    (28, N'Phong', 'phong'), (29, N'Phúc', 'phuc'), (30, N'Quân', 'quan'),  (31, N'Tâm', 'tam'),
    (32, N'Thảo', 'thao'),  (33, N'Trang', 'trang');

;WITH numbers AS (
    SELECT TOP (500) ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS n
    FROM sys.all_objects
),
people AS (
    SELECT
        numbers.n,
        f.name + N' ' + m.name + N' ' + g.name AS full_name,
        g.slug + '.' + f.slug + RIGHT('00' + CAST(numbers.n AS VARCHAR(3)), 3) + '@student.edu.vn' AS email,
        -- Birthdays between 2001-01-01 and 2005-12-31, stable for the same row number
        DATEADD(DAY, (numbers.n * 37) % 1826, CAST('2001-01-01' AS DATE)) AS date_of_birth
    FROM numbers
    JOIN @family AS f ON f.idx = (numbers.n - 1) % 15
    JOIN @given  AS g ON g.idx = ((numbers.n - 1) / 15) % 34
    JOIN @middle AS m ON m.idx = (numbers.n * 7) % 10
)
INSERT INTO students (full_name, email, date_of_birth)
SELECT full_name, email, date_of_birth
FROM people
WHERE NOT EXISTS (SELECT 1 FROM students AS s WHERE s.email = people.email);
GO

-- 3. Student accounts for the first 49 mock students.
--    Username is the email name, e.g. an.nguyen001 / password123.
INSERT INTO users (username, password, role, student_id)
SELECT TOP (49) LEFT(s.email, CHARINDEX('@', s.email) - 1), 'password123', 'STUDENT', s.student_id
FROM students AS s
WHERE s.email LIKE '%[0-9][0-9][0-9]@student.edu.vn'
  AND NOT EXISTS (SELECT 1 FROM users AS u WHERE u.student_id = s.student_id)
  AND NOT EXISTS (SELECT 1 FROM users AS u WHERE u.username = LEFT(s.email, CHARINDEX('@', s.email) - 1))
ORDER BY s.student_id;
GO

-- 4. Up to 500 random enrollments in total, never above a course's capacity
--    (existing enrollments are counted too).
;WITH currentCounts AS (
    SELECT course_id, COUNT(*) AS enrolled
    FROM enrollments
    GROUP BY course_id
),
candidates AS (
    SELECT
        s.student_id,
        c.course_id,
        ROW_NUMBER() OVER (PARTITION BY c.course_id ORDER BY NEWID()) AS course_row,
        c.capacity - ISNULL(cc.enrolled, 0) AS seats_left
    FROM students AS s
    CROSS JOIN courses AS c
    LEFT JOIN currentCounts AS cc ON cc.course_id = c.course_id
    WHERE s.active = 1
      AND NOT EXISTS (
        SELECT 1
        FROM enrollments AS e
        WHERE e.student_id = s.student_id
          AND e.course_id = c.course_id
    )
),
availableEnrollments AS (
    SELECT
        student_id,
        course_id,
        ROW_NUMBER() OVER (ORDER BY NEWID()) AS row_num
    FROM candidates
    WHERE course_row <= seats_left
)
INSERT INTO enrollments (student_id, course_id, enrollment_date)
SELECT
    student_id,
    course_id,
    -- Registered some time in the last 90 days
    DATEADD(DAY, -ABS(CHECKSUM(NEWID()) % 90), CAST(GETDATE() AS DATE))
FROM availableEnrollments
WHERE row_num <= CASE
    WHEN (SELECT COUNT(*) FROM enrollments) < 500
        THEN 500 - (SELECT COUNT(*) FROM enrollments)
    ELSE 0
END;
GO

-- Row counts after seeding
SELECT 'users' AS table_name, COUNT(*) AS total FROM users
UNION ALL SELECT 'students', COUNT(*) FROM students
UNION ALL SELECT 'courses', COUNT(*) FROM courses
UNION ALL SELECT 'enrollments', COUNT(*) FROM enrollments;
GO
