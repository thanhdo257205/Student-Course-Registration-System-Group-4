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
    role          NVARCHAR(20)  NOT NULL DEFAULT 'ADMIN'
);

CREATE TABLE students (
    student_id    INT IDENTITY(1,1) PRIMARY KEY,
    full_name     NVARCHAR(100) NOT NULL,
    email         NVARCHAR(100) NOT NULL UNIQUE,
    date_of_birth DATE          NOT NULL
);

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
INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN');
GO
