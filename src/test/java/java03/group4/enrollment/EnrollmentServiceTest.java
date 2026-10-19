package java03.group4.enrollment;

import java03.group4.course.Course;
import java03.group4.course.CourseRepository;
import java03.group4.student.Student;
import java03.group4.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EnrollmentServiceTest {

    private final CourseRepository courses = mock(CourseRepository.class);
    private final StudentRepository students = mock(StudentRepository.class);
    private final EnrollmentRepository enrollments = mock(EnrollmentRepository.class);
    private final EnrollmentService service = new EnrollmentService(courses, students, enrollments);

    @BeforeEach
    void setUp() {
        Course course = new Course("Algorithms", "Lan Pham", 2);
        ReflectionTestUtils.setField(course, "courseId", 1);
        when(courses.findByIdForUpdate(1)).thenReturn(Optional.of(course));
        when(students.findById(7)).thenReturn(Optional.of(
                new Student("Minh Nguyen", "minh@example.edu", LocalDate.of(2004, 5, 12))));
    }

    @Test
    void registersWhenSeatsLeft() {
        when(enrollments.countByCourse_CourseId(1)).thenReturn(1L);
        service.register(7, 1);
        verify(enrollments).save(any());
    }

    @Test
    void rejectsFullCourse() {
        when(enrollments.countByCourse_CourseId(1)).thenReturn(2L);
        assertThrows(IllegalArgumentException.class, () -> service.register(7, 1));
        verify(enrollments, never()).save(any());
    }

    @Test
    void rejectsDuplicate() {
        when(enrollments.existsByStudent_StudentIdAndCourse_CourseId(7, 1)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.register(7, 1));
        verify(enrollments, never()).save(any());
    }

    @Test
    void rejectsInactiveStudent() {
        Student inactive = new Student("Lan Tran", "lan@example.edu", LocalDate.of(2004, 7, 22));
        inactive.setActive(false);
        when(students.findById(8)).thenReturn(Optional.of(inactive));

        assertThrows(IllegalArgumentException.class, () -> service.register(8, 1));
        verify(enrollments, never()).save(any());
    }

    @Test
    void rejectsUnknownStudent() {
        assertThrows(IllegalArgumentException.class, () -> service.register(99, 1));
    }
}
