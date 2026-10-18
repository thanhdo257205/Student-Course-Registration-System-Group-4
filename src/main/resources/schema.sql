IF COL_LENGTH('dbo.users', 'student_id') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD student_id INT NULL;
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'fk_users_student'
      AND parent_object_id = OBJECT_ID('dbo.users')
)
BEGIN
    ALTER TABLE dbo.users
        ADD CONSTRAINT fk_users_student FOREIGN KEY (student_id) REFERENCES dbo.students(student_id);
END;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'ux_users_student_id'
      AND object_id = OBJECT_ID('dbo.users')
)
BEGIN
    CREATE UNIQUE INDEX ux_users_student_id ON dbo.users(student_id) WHERE student_id IS NOT NULL;
END;
