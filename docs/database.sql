-- Chay file nay trong SSMS / Azure Data Studio tren SQL Server.
-- CANH BAO: moi lan chay se xoa database student_course_db va toan bo du lieu hien co.
USE master;
GO

IF DB_ID('student_course_db') IS NOT NULL
BEGIN
    ALTER DATABASE student_course_db SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE student_course_db;
END
GO

CREATE DATABASE student_course_db;
GO
USE student_course_db;
GO

CREATE TABLE users (
    user_id       INT IDENTITY(1,1) PRIMARY KEY,
    username      NVARCHAR(50)  NOT NULL UNIQUE,
    password      NVARCHAR(255) NOT NULL,
    role          NVARCHAR(20)  NOT NULL DEFAULT 'ADMIN',
    student_id    INT           NULL
);

CREATE TABLE students (
    student_id    INT IDENTITY(1,1) PRIMARY KEY,
    full_name     NVARCHAR(100) NOT NULL,
    email         NVARCHAR(100) NOT NULL UNIQUE,
    date_of_birth DATE          NOT NULL,
    active        BIT           NOT NULL CONSTRAINT df_students_active DEFAULT 1
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_student FOREIGN KEY (student_id) REFERENCES students(student_id);
CREATE UNIQUE INDEX ux_users_student_id ON users(student_id) WHERE student_id IS NOT NULL;

CREATE TABLE courses (
    course_id       INT IDENTITY(1,1) PRIMARY KEY,
    course_name     NVARCHAR(100) NOT NULL,
    instructor_name NVARCHAR(100) NOT NULL,
    capacity        INT           NOT NULL CHECK (capacity > 0)
);

CREATE TABLE enrollments (
    enrollment_id   INT IDENTITY(1,1) PRIMARY KEY,
    student_id      INT  NOT NULL,
    course_id       INT  NOT NULL,
    enrollment_date DATE NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES students(student_id),
    CONSTRAINT fk_enrollments_course  FOREIGN KEY (course_id)  REFERENCES courses(course_id),
    CONSTRAINT uq_student_course UNIQUE (student_id, course_id)
);
GO

-- Tai khoan mau (mat khau chua ma hoa, chi dung de hoc)
INSERT INTO users (username, password, role) VALUES
    ('admin', 'admin123', 'ADMIN'),
    ('schoolstaff', 'staff123', 'SCHOOL_STAFF');

-- Sinh vien mau: tai khoan 'minh.nguyen' gan voi Nguyen Van Minh
INSERT INTO students (full_name, email, date_of_birth) VALUES
    (N'Nguyễn Văn Minh', 'minh.nguyen@student.edu.vn', '2004-03-15'),
    (N'Trần Thị Lan',    'lan.tran@student.edu.vn',    '2004-07-22'),
    (N'Lê Hoàng Nam',    'nam.le@student.edu.vn',      '2003-11-05');

INSERT INTO users (username, password, role, student_id)
SELECT 'minh.nguyen', 'minh123', 'STUDENT', student_id
FROM students WHERE email = 'minh.nguyen@student.edu.vn';

INSERT INTO courses (course_name, instructor_name, capacity) VALUES
    (N'Introduction to Programming', N'Nguyễn Văn An', 30),
    (N'Cơ sở dữ liệu', N'Trần Thị Bình', 40),
    -- Khoa hoc da full (2/2) de test truong hop het cho
    (N'Machine Learning Workshop', N'Phạm Minh Đức', 2);

-- Minh da dang ky san 1 khoa; Lan va Nam lap day khoa Machine Learning Workshop
INSERT INTO enrollments (student_id, course_id, enrollment_date)
SELECT s.student_id, c.course_id, seed.enrollment_date
FROM (VALUES
    ('minh.nguyen@student.edu.vn', N'Introduction to Programming', CAST('2026-09-01' AS DATE)),
    ('lan.tran@student.edu.vn',    N'Machine Learning Workshop',   CAST('2026-09-02' AS DATE)),
    ('nam.le@student.edu.vn',      N'Machine Learning Workshop',   CAST('2026-09-03' AS DATE)),
    ('lan.tran@student.edu.vn',    N'Cơ sở dữ liệu',               CAST('2026-09-04' AS DATE))
) AS seed (email, course_name, enrollment_date)
JOIN students AS s ON s.email = seed.email
JOIN courses AS c ON c.course_name = seed.course_name;
GO
