USE student_course_db;
GO

-- 1. Thêm 50 bản ghi vào bảng users (Bỏ qua nếu đã có admin)
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')
    INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN');

DECLARE @i INT = 1;
WHILE @i <= 49
BEGIN
    INSERT INTO users (username, password, role)
    SELECT 'user' + CAST(@i AS VARCHAR), 'password123', CASE WHEN @i % 5 = 0 THEN 'ADMIN' ELSE 'USER' END
    WHERE NOT EXISTS (
        SELECT 1
        FROM users
        WHERE username = 'user' + CAST(@i AS VARCHAR)
    );
    SET @i = @i + 1;
END
GO

-- 2. Thêm 30 khóa học vào bảng courses
DECLARE @courses TABLE (course_name NVARCHAR(100), instructor_name NVARCHAR(100), capacity INT);
INSERT INTO @courses VALUES
(N'Lập trình C# cơ bản', N'Nguyễn Văn An', 40),
(N'Cơ sở dữ liệu nâng cao', N'Trần Thị Bình', 35),
(N'Phát triển Web với ReactJS', N'Lê Hoàng Cường', 50),
(N'Thuật toán và Cấu trúc dữ liệu', N'Phạm Minh Đức', 45),
(N'Hệ điều hành Linux', N'Hoàng Văn Em', 30),
(N'Trí tuệ nhân tạo cơ bản', N'Vũ Thị Phương', 40),
(N'An toàn bảo mật thông tin', N'Đỗ Quốc Huy', 35),
(N'Lập trình Java Enterprise', N'Nguyễn Thị Mai', 50),
(N'Thiết kế giao diện UI/UX', N'Bùi Thanh Nga', 30),
(N'Điện toán đám mây AWS', N'Phan Văn Phúc', 40),
(N'Phát triển ứng dụng di động Flutter', N'Võ Thị Quỳnh', 45),
(N'Kiểm thử phần mềm (Testing)', N'Ngô Văn Sơn', 35),
(N'Big Data và Phân tích dữ liệu', N'Dương Thị Thùy', 40),
(N'Quản trị dự án Agile/Scrum', N'Lý Minh Tuấn', 30),
(N'Lập trình Python cho Data Science', N'Đinh Thị Uyên', 50),
(N'Mạng máy tính nâng cao', N'Mai Văn Vinh', 35),
(N'Kiến trúc vi dịch vụ (Microservices)', N'Tạ Thị Xuân', 40),
(N'Công nghệ Blockchain', N'Hồ Văn Yên', 30),
(N'Thương mại điện tử', N'Trương Thị Zâm', 45),
(N'Lập trình C++ hướng đối tượng', N'Lương Văn Bảo', 40),
(N'Phát triển Web Backend với Node.js', N'Cao Thị Châu', 50),
(N'Lập trình game Unity', N'Đàm Văn Dũng', 35),
(N'Toán rời rạc cho CNTT', N'Hà Thị Giang', 45),
(N'Nhập môn Kỹ thuật phần mềm', N'Khuất Văn Hùng', 40),
(N'Xử lý ảnh số và Computer Vision', N'Lại Thị Kiều', 30),
(N'Lập trình nhúng IoT', N'Mạch Văn Long', 35),
(N'Kinh tế chính trị Mác - Lênin', N'Nghiêm Thị Mỹ', 60),
(N'Pháp luật đại cương', N'Phùng Văn Nam', 60),
(N'Tư tưởng Hồ Chí Minh', N'Quách Thị Oanh', 60),
(N'Tiếng Anh chuyên ngành CNTT', N'Sầm Văn Phong', 50);

INSERT INTO courses (course_name, instructor_name, capacity)
SELECT seed.course_name, seed.instructor_name, seed.capacity
FROM @courses AS seed
WHERE NOT EXISTS (
    SELECT 1
    FROM courses AS existing
    WHERE existing.course_name = seed.course_name
      AND existing.instructor_name = seed.instructor_name
);
GO

-- 3. Thêm 500 sinh viên vào bảng students
SET NOCOUNT ON;
DECLARE @n INT = 1;

WHILE @n <= 500
BEGIN
    DECLARE @fullName NVARCHAR(100);
    SET @fullName =
        CASE @n % 10
            WHEN 0 THEN N'Nguyễn Văn '
            WHEN 1 THEN N'Trần Thị '
            WHEN 2 THEN N'Lê Hoàng '
            WHEN 3 THEN N'Phạm Minh '
            WHEN 4 THEN N'Hoàng Văn '
            WHEN 5 THEN N'Vũ Thị '
            WHEN 6 THEN N'Đỗ Gia '
            WHEN 7 THEN N'Bùi Thanh '
            WHEN 8 THEN N'Ngô Đức '
            ELSE N'Hồ Ngọc '
        END +
        CASE @n % 15
            WHEN 0 THEN N'An'
            WHEN 1 THEN N'Bình'
            WHEN 2 THEN N'Cường'
            WHEN 3 THEN N'Dũng'
            WHEN 4 THEN N'Hà'
            WHEN 5 THEN N'Hải'
            WHEN 6 THEN N'Hiếu'
            WHEN 7 THEN N'Huy'
            WHEN 8 THEN N'Linh'
            WHEN 9 THEN N'Long'
            WHEN 10 THEN N'Mai'
            WHEN 11 THEN N'Nam'
            WHEN 12 THEN N'Phong'
            WHEN 13 THEN N'Phương'
            ELSE N'Tuấn'
        END +
        N' ' + CONVERT(NVARCHAR(10), @n);

    -- Tạo email không trùng lặp
    DECLARE @email NVARCHAR(100) = 'student' + CAST(@n AS VARCHAR) + '@student.edu.vn';
    
    -- Ngày sinh ngẫu nhiên trong khoảng từ 2000 đến 2005
    DECLARE @dob DATE = DATEADD(DAY, ABS(CHECKSUM(NEWID()) % 1800), '2000-01-01');

    IF NOT EXISTS (SELECT 1 FROM students WHERE email = @email)
    BEGIN
        INSERT INTO students (full_name, email, date_of_birth)
        VALUES (@fullName, @email, @dob);
    END
    SET @n = @n + 1;
END
GO

-- 4. Thêm tối đa 500 lượt đăng ký ngẫu nhiên cho các sinh viên và khóa học hiện có.
;WITH availableEnrollments AS (
    SELECT
        s.student_id,
        c.course_id,
        ROW_NUMBER() OVER (ORDER BY NEWID()) AS row_num
    FROM students AS s
    CROSS JOIN courses AS c
    WHERE NOT EXISTS (
        SELECT 1
        FROM enrollments AS e
        WHERE e.student_id = s.student_id
          AND e.course_id = c.course_id
    )
)
INSERT INTO enrollments (student_id, course_id, enrollment_date)
SELECT
    student_id,
    course_id,
    DATEADD(DAY, -ABS(CHECKSUM(NEWID()) % 90), GETDATE())
FROM availableEnrollments
WHERE row_num <= CASE
    WHEN (SELECT COUNT(*) FROM enrollments) < 500
        THEN 500 - (SELECT COUNT(*) FROM enrollments)
    ELSE 0
END;
GO

-- Kiểm tra kết quả số lượng bản ghi đã tạo
SELECT 'users' AS TableName, COUNT(*) AS Total FROM users
UNION ALL
SELECT 'students', COUNT(*) FROM students
UNION ALL
SELECT 'courses', COUNT(*) FROM courses
UNION ALL
SELECT 'enrollments', COUNT(*) FROM enrollments;
GO
