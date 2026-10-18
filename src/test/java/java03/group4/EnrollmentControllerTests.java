package java03.group4;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class EnrollmentControllerTests {

    @Test
    void rejectsEnrollmentWhenCourseIsAtCapacity() {
        RecordStudentRepository students = mock(RecordStudentRepository.class);
        RecordCourseRepository courses = mock(RecordCourseRepository.class);
        RecordEnrollmentRepository enrollments = mock(RecordEnrollmentRepository.class);
        Student student = new Student("Minh Nguyen", "minh@example.edu", LocalDate.of(2004, 5, 12));
        Course course = new Course("Algorithms", "Lan Pham", 1);
        EnrollmentForm form = new EnrollmentForm(1, 2, LocalDate.now());
        var bindingResult = new BeanPropertyBindingResult(form, "enrollmentForm");

        when(students.findById(1)).thenReturn(Optional.of(student));
        when(courses.findById(2)).thenReturn(Optional.of(course));
        when(students.findAll()).thenReturn(List.of(student));
        when(courses.findAll()).thenReturn(List.of(course));
        when(enrollments.countByCourse_CourseId(2)).thenReturn(1L);

        String view = new EnrollmentController(students, courses, enrollments).createEnrollment(
                form, bindingResult, new ExtendedModelMap(), new RedirectAttributesModelMap());

        assertEquals("enrollment-form", view);
        assertTrue(bindingResult.hasFieldErrors("courseId"));
        verify(enrollments, never()).save(any(Enrollment.class));
    }

    @Test
    void rejectsDuplicateStudentAndCoursePair() {
        RecordStudentRepository students = mock(RecordStudentRepository.class);
        RecordCourseRepository courses = mock(RecordCourseRepository.class);
        RecordEnrollmentRepository enrollments = mock(RecordEnrollmentRepository.class);
        Student student = new Student("Minh Nguyen", "minh@example.edu", LocalDate.of(2004, 5, 12));
        Course course = new Course("Algorithms", "Lan Pham", 4);
        EnrollmentForm form = new EnrollmentForm(1, 2, LocalDate.now());
        var bindingResult = new BeanPropertyBindingResult(form, "enrollmentForm");

        when(students.findById(1)).thenReturn(Optional.of(student));
        when(courses.findById(2)).thenReturn(Optional.of(course));
        when(students.findAll()).thenReturn(List.of(student));
        when(courses.findAll()).thenReturn(List.of(course));
        when(enrollments.existsByStudent_StudentIdAndCourse_CourseId(1, 2)).thenReturn(true);

        String view = new EnrollmentController(students, courses, enrollments).createEnrollment(
                form, bindingResult, new ExtendedModelMap(), new RedirectAttributesModelMap());

        assertEquals("enrollment-form", view);
        assertTrue(bindingResult.hasGlobalErrors());
        verify(enrollments, never()).save(any(Enrollment.class));
    }
}
