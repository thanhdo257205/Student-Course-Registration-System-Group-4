IF COL_LENGTH(N'dbo.users', N'student_id') IS NULL
    EXEC(N'ALTER TABLE dbo.users ADD student_id INT NULL');

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'fk_users_student'
      AND parent_object_id = OBJECT_ID('dbo.users')
)
    EXEC(N'ALTER TABLE dbo.users
        ADD CONSTRAINT fk_users_student FOREIGN KEY (student_id) REFERENCES dbo.students(student_id)');

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'ux_users_student_id'
      AND object_id = OBJECT_ID('dbo.users')
)
    EXEC(N'CREATE UNIQUE INDEX ux_users_student_id ON dbo.users(student_id) WHERE student_id IS NOT NULL');
