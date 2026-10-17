package java03.group4.service;

import java03.group4.entity.Course;
import java03.group4.entity.Student;
import java03.group4.repository.CourseRepository;
import java03.group4.repository.EnrollmentRepository;
import java03.group4.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        Course course = new Course();
        course.setId(1);
        course.setCapacity(2);
        when(courses.findById(1)).thenReturn(Optional.of(course));
        when(students.findById(7)).thenReturn(Optional.of(new Student()));
    }

    @Test
    void registersWhenSeatsLeft() {
        when(enrollments.countByCourse_Id(1)).thenReturn(1L);
        service.register(7, 1);
        verify(enrollments).save(any());
    }

    @Test
    void rejectsFullCourse() {
        when(enrollments.countByCourse_Id(1)).thenReturn(2L);
        assertThrows(IllegalArgumentException.class, () -> service.register(7, 1));
        verify(enrollments, never()).save(any());
    }

    @Test
    void rejectsDuplicate() {
        when(enrollments.existsByStudent_IdAndCourse_Id(7, 1)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.register(7, 1));
        verify(enrollments, never()).save(any());
    }

    @Test
    void rejectsUnknownStudent() {
        assertThrows(IllegalArgumentException.class, () -> service.register(99, 1));
    }
}
