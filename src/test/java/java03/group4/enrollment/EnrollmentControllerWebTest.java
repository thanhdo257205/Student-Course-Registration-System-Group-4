package java03.group4.enrollment;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java03.group4.auth.User;
import java03.group4.course.Course;
import java03.group4.course.CourseRepository;
import java03.group4.student.Student;
import java03.group4.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

/** Test case E01-E16: man hinh quan ly dang ky cua staff (/enrollments). */
@WebMvcTest(EnrollmentController.class)
class EnrollmentControllerWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StudentRepository students;

    @MockBean
    private CourseRepository courses;

    @MockBean
    private EnrollmentRepository enrollments;

    private Student minh;
    private Course algorithms;
    private Course databases;

    @BeforeEach
    void seed() {
        minh = new Student("Minh Nguyen", "minh@example.edu", LocalDate.of(2004, 5, 12));
        ReflectionTestUtils.setField(minh, "studentId", 1);
        algorithms = new Course("Algorithms", "Lan Pham", 2);
        ReflectionTestUtils.setField(algorithms, "courseId", 10);
        databases = new Course("Databases", "Hoa Tran", 2);
        ReflectionTestUtils.setField(databases, "courseId", 20);

        when(students.findById(1)).thenReturn(Optional.of(minh));
        when(courses.findById(10)).thenReturn(Optional.of(algorithms));
        when(courses.findById(20)).thenReturn(Optional.of(databases));
        when(students.findAll()).thenReturn(List.of(minh));
        when(courses.findAll()).thenReturn(List.of(algorithms, databases));
    }

    private MockHttpSession staff() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", new User("schoolstaff", "staff123", "SCHOOL_STAFF"));
        return session;
    }

    private Enrollment storedEnrollment(int id, Course course) {
        Enrollment enrollment = new Enrollment(minh, course, LocalDate.of(2026, 3, 15));
        ReflectionTestUtils.setField(enrollment, "enrollmentId", id);
        when(enrollments.findById(id)).thenReturn(Optional.of(enrollment));
        return enrollment;
    }

    @Test
    void e01_newFormRedirectsWhenThereAreNoStudents() throws Exception {
        when(students.findAll()).thenReturn(List.of());

        mvc.perform(get("/enrollments/new").session(staff()))
                .andExpect(redirectedUrl("/enrollments"))
                .andExpect(flash().attribute("errorMessage",
                        "Add at least one student and one course before creating an enrollment."));
    }

    @Test
    void e02_newFormListsStudentsAndCoursesWithTodayAsDefaultDate() throws Exception {
        mvc.perform(get("/enrollments/new").session(staff()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Minh Nguyen")))
                .andExpect(content().string(containsString("Algorithms (capacity 2)")))
                .andExpect(content().string(containsString("value=\"" + LocalDate.now() + "\"")));
    }

    @Test
    void e03_createValidEnrollment() throws Exception {
        mvc.perform(post("/enrollments").session(staff())
                        .param("studentId", "1").param("courseId", "10").param("enrollmentDate", "2026-03-15"))
                .andExpect(redirectedUrl("/enrollments"))
                .andExpect(flash().attribute("successMessage", "Enrollment record created."));

        ArgumentCaptor<Enrollment> saved = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollments).save(saved.capture());
        assertSame(minh, saved.getValue().getStudent());
        assertSame(algorithms, saved.getValue().getCourse());
        assertEquals(LocalDate.of(2026, 3, 15), saved.getValue().getEnrollmentDate());
    }

    @Test
    void e04_createWithMissingFieldsShowsErrors() throws Exception {
        mvc.perform(post("/enrollments").session(staff()))
                .andExpect(status().isOk())
                .andExpect(view().name("enrollment-form"))
                .andExpect(model().attributeHasFieldErrors("enrollmentForm", "studentId", "courseId", "enrollmentDate"));

        verify(enrollments, never()).save(any());
    }

    @Test
    void e05_createRejectsFutureDate() throws Exception {
        mvc.perform(post("/enrollments").session(staff())
                        .param("studentId", "1").param("courseId", "10")
                        .param("enrollmentDate", LocalDate.now().plusDays(1).toString()))
                .andExpect(view().name("enrollment-form"))
                .andExpect(model().attributeHasFieldErrors("enrollmentForm", "enrollmentDate"));

        verify(enrollments, never()).save(any());
    }

    @Test
    void e06_createRejectsUnknownStudentAndCourse() throws Exception {
        when(students.findById(99)).thenReturn(Optional.empty());
        when(courses.findById(98)).thenReturn(Optional.empty());

        mvc.perform(post("/enrollments").session(staff())
                        .param("studentId", "99").param("courseId", "98").param("enrollmentDate", "2026-03-15"))
                .andExpect(view().name("enrollment-form"))
                .andExpect(model().attributeHasFieldErrorCode("enrollmentForm", "studentId", "notFound"))
                .andExpect(model().attributeHasFieldErrorCode("enrollmentForm", "courseId", "notFound"));

        verify(enrollments, never()).save(any());
    }

    @Test
    void e07_createRejectsNonNumericIdsWithoutCrashing() throws Exception {
        mvc.perform(post("/enrollments").session(staff())
                        .param("studentId", "abc").param("courseId", "10").param("enrollmentDate", "2026-03-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("enrollment-form"))
                .andExpect(model().attributeHasFieldErrors("enrollmentForm", "studentId"));

        verify(enrollments, never()).save(any());
    }

    @Test
    void e08_createDuplicateShowsMessageOnPage() throws Exception {
        when(enrollments.existsByStudent_StudentIdAndCourse_CourseId(1, 10)).thenReturn(true);

        mvc.perform(post("/enrollments").session(staff())
                        .param("studentId", "1").param("courseId", "10").param("enrollmentDate", "2026-03-15"))
                .andExpect(view().name("enrollment-form"))
                .andExpect(content().string(containsString("This student is already enrolled in this course.")));

        verify(enrollments, never()).save(any());
    }

    @Test
    void e09_createRejectsFullCourse() throws Exception {
        when(enrollments.countByCourse_CourseId(10)).thenReturn(2L);

        mvc.perform(post("/enrollments").session(staff())
                        .param("studentId", "1").param("courseId", "10").param("enrollmentDate", "2026-03-15"))
                .andExpect(view().name("enrollment-form"))
                .andExpect(model().attributeHasFieldErrorCode("enrollmentForm", "courseId", "full"));

        verify(enrollments, never()).save(any());
    }

    @Test
    void e10_editFormIsPrefilledWithStoredValues() throws Exception {
        storedEnrollment(5, algorithms);

        mvc.perform(get("/enrollments/5/edit").session(staff()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"2026-03-15\"")))
                .andExpect(content().string(containsString("<option value=\"10\" selected=\"selected\"")));
    }

    @Test
    void e11_updateInSameFullCourseIsAllowed() throws Exception {
        Enrollment stored = storedEnrollment(5, algorithms);
        when(enrollments.countByCourse_CourseId(10)).thenReturn(2L);

        mvc.perform(post("/enrollments/5").session(staff())
                        .param("studentId", "1").param("courseId", "10").param("enrollmentDate", "2026-03-01"))
                .andExpect(redirectedUrl("/enrollments"))
                .andExpect(flash().attribute("successMessage", "Enrollment record updated."));

        verify(enrollments).save(stored);
        assertEquals(LocalDate.of(2026, 3, 1), stored.getEnrollmentDate());
    }

    @Test
    void e12_updateCannotMoveIntoFullCourse() throws Exception {
        Enrollment stored = storedEnrollment(5, algorithms);
        when(enrollments.countByCourse_CourseId(20)).thenReturn(2L);

        mvc.perform(post("/enrollments/5").session(staff())
                        .param("studentId", "1").param("courseId", "20").param("enrollmentDate", "2026-03-15"))
                .andExpect(view().name("enrollment-form"))
                .andExpect(model().attributeHasFieldErrorCode("enrollmentForm", "courseId", "full"));

        verify(enrollments, never()).save(any());
        assertSame(algorithms, stored.getCourse());
    }

    @Test
    void e13_updateCannotCreateDuplicatePair() throws Exception {
        storedEnrollment(5, algorithms);
        when(enrollments.existsByStudent_StudentIdAndCourse_CourseIdAndEnrollmentIdNot(1, 20, 5)).thenReturn(true);

        mvc.perform(post("/enrollments/5").session(staff())
                        .param("studentId", "1").param("courseId", "20").param("enrollmentDate", "2026-03-15"))
                .andExpect(view().name("enrollment-form"))
                .andExpect(content().string(containsString("This student is already enrolled in this course.")));

        verify(enrollments, never()).save(any());
    }

    @Test
    void e14_updateOrDeleteUnknownEnrollmentShowsError() throws Exception {
        when(enrollments.findById(99)).thenReturn(Optional.empty());

        mvc.perform(post("/enrollments/99").session(staff())
                        .param("studentId", "1").param("courseId", "10").param("enrollmentDate", "2026-03-15"))
                .andExpect(redirectedUrl("/enrollments"))
                .andExpect(flash().attribute("errorMessage", "Enrollment record was not found."));
        mvc.perform(post("/enrollments/99/delete").session(staff()))
                .andExpect(redirectedUrl("/enrollments"))
                .andExpect(flash().attribute("errorMessage", "Enrollment record was not found."));

        verify(enrollments, never()).save(any());
        verify(enrollments, never()).delete(any());
    }

    @Test
    void e15_deleteExistingEnrollment() throws Exception {
        Enrollment stored = storedEnrollment(5, algorithms);

        mvc.perform(post("/enrollments/5/delete").session(staff()))
                .andExpect(redirectedUrl("/enrollments"))
                .andExpect(flash().attribute("successMessage", "Enrollment record deleted."));

        verify(enrollments).delete(stored);
    }

    @Test
    void e16_listSearchIsTrimmedAndRendersRows() throws Exception {
        Enrollment stored = storedEnrollment(5, algorithms);
        when(enrollments.search(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(stored)));

        mvc.perform(get("/enrollments").session(staff()).param("q", "  minh "))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Minh Nguyen")))
                .andExpect(content().string(containsString("Algorithms")))
                .andExpect(content().string(containsString("2026-03-15")))
                .andExpect(content().string(containsString("placeholder=\"Search by student name, email, or course\"")));

        verify(enrollments).search(eq("minh"), any(Pageable.class));
    }

    @Test
    void e19_searchQueryDoesNotMatchInstructorName() throws Exception {
        // Repository bi mock o day, nen kiem tra thang cau JPQL: tim "Minh" khong duoc ra khoa co giang vien ten Minh
        var query = EnrollmentRepository.class.getMethod("search", String.class, Pageable.class)
                .getAnnotation(org.springframework.data.jpa.repository.Query.class);

        for (String jpql : List.of(query.value(), query.countQuery())) {
            org.junit.jupiter.api.Assertions.assertFalse(jpql.contains("instructorName"), jpql);
            org.junit.jupiter.api.Assertions.assertTrue(jpql.contains("e.student.fullName"), jpql);
            org.junit.jupiter.api.Assertions.assertTrue(jpql.contains("e.student.email"), jpql);
            org.junit.jupiter.api.Assertions.assertTrue(jpql.contains("e.course.courseName"), jpql);
        }
    }

    @Test
    void e17_listIsSortedOldestEnrollmentFirst() throws Exception {
        when(enrollments.search(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mvc.perform(get("/enrollments").session(staff())).andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(enrollments).search(eq(""), pageable.capture());
        assertEquals(org.springframework.data.domain.Sort.Direction.ASC,
                pageable.getValue().getSort().getOrderFor("enrollmentDate").getDirection());
    }

    @Test
    void e18_inactiveStudentIsHiddenFromOptionsAndRejected() throws Exception {
        Student lan = new Student("Lan Tran", "lan@example.edu", LocalDate.of(2004, 7, 22));
        ReflectionTestUtils.setField(lan, "studentId", 2);
        lan.setActive(false);
        when(students.findAll()).thenReturn(List.of(minh, lan));
        when(students.findById(2)).thenReturn(Optional.of(lan));

        mvc.perform(get("/enrollments/new").session(staff()))
                .andExpect(content().string(containsString("Minh Nguyen")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Lan Tran"))));

        mvc.perform(post("/enrollments").session(staff())
                        .param("studentId", "2").param("courseId", "10").param("enrollmentDate", "2026-03-15"))
                .andExpect(view().name("enrollment-form"))
                .andExpect(model().attributeHasFieldErrorCode("enrollmentForm", "studentId", "inactive"));

        verify(enrollments, never()).save(any());
    }
}
