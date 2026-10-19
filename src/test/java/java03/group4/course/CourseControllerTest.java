package java03.group4.course;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

import java.util.List;
import java.util.Optional;
import java03.group4.auth.User;
import java03.group4.enrollment.CourseEnrollmentSummary;
import java03.group4.enrollment.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

/** Test case C01-C14: man hinh quan ly khoa hoc cua staff (/courses). */
@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CourseRepository courses;

    @MockBean
    private EnrollmentRepository enrollments;

    private MockHttpSession staff() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", new User("admin", "admin123", "ADMIN"));
        return session;
    }

    private MockHttpSession student() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", new User("student", "student123", "STUDENT"));
        return session;
    }

    private Course course(int id, String name, int capacity) {
        Course course = new Course(name, "Lan Pham", capacity);
        ReflectionTestUtils.setField(course, "courseId", id);
        return course;
    }

    private static CourseEnrollmentSummary summary(int courseId, long count) {
        return new CourseEnrollmentSummary() {
            public Integer getCourseId() { return courseId; }
            public Long getEnrollmentCount() { return count; }
        };
    }

    @Test
    void c01_listShowsEnrollmentCountPerCourseAndZeroForEmptyCourse() throws Exception {
        when(courses.search(eq(""), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(course(1, "Algorithms", 30), course(2, "Databases", 20)),
                        PageRequest.of(0, 10), 2));
        when(enrollments.countPerCourse(anyList())).thenReturn(List.of(summary(1, 5)));

        mvc.perform(get("/courses").session(staff()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<td data-label=\"Enrolled\">5</td>")))
                .andExpect(content().string(containsString("<td data-label=\"Enrolled\">0</td>")));
    }

    @Test
    void c02_searchKeywordIsTrimmedBeforeQuerying() throws Exception {
        when(courses.search(anyString(), any(Pageable.class))).thenReturn(org.springframework.data.domain.Page.empty());

        mvc.perform(get("/courses").session(staff()).param("q", "  Java  "))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No courses match your search.")));

        verify(courses).search(eq("Java"), any(Pageable.class));
    }

    @Test
    void c03_createValidCourseSavesTrimmedValues() throws Exception {
        mvc.perform(post("/courses").session(staff())
                        .param("courseName", "  Algorithms ")
                        .param("instructorName", " Lan Pham ")
                        .param("capacity", "30"))
                .andExpect(redirectedUrl("/courses"))
                .andExpect(flash().attribute("successMessage", "Course record created."));

        ArgumentCaptor<Course> saved = ArgumentCaptor.forClass(Course.class);
        verify(courses).save(saved.capture());
        assertEquals("Algorithms", saved.getValue().getCourseName());
        assertEquals("Lan Pham", saved.getValue().getInstructorName());
        assertEquals(30, saved.getValue().getCapacity());
    }

    @Test
    void c04_createRejectsBlankNamesAndZeroCapacity() throws Exception {
        mvc.perform(post("/courses").session(staff())
                        .param("courseName", "   ")
                        .param("instructorName", "")
                        .param("capacity", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("course-form"))
                .andExpect(model().attributeHasFieldErrors("courseForm", "courseName", "instructorName", "capacity"));

        verify(courses, never()).save(any());
    }

    @Test
    void c05_createRejectsNonNumericCapacityWithoutCrashing() throws Exception {
        mvc.perform(post("/courses").session(staff())
                        .param("courseName", "Algorithms")
                        .param("instructorName", "Lan Pham")
                        .param("capacity", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("course-form"))
                .andExpect(model().attributeHasFieldErrors("courseForm", "capacity"));

        verify(courses, never()).save(any());
    }

    @Test
    void c06_createRejectsNameLongerThan100Characters() throws Exception {
        mvc.perform(post("/courses").session(staff())
                        .param("courseName", "x".repeat(101))
                        .param("instructorName", "Lan Pham")
                        .param("capacity", "30"))
                .andExpect(view().name("course-form"))
                .andExpect(model().attributeHasFieldErrors("courseForm", "courseName"));

        verify(courses, never()).save(any());
    }

    @Test
    void c07_editFormIsPrefilledWithStoredValues() throws Exception {
        when(courses.findById(1)).thenReturn(Optional.of(course(1, "Algorithms", 30)));

        mvc.perform(get("/courses/1/edit").session(staff()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"Algorithms\"")))
                .andExpect(content().string(containsString("value=\"30\"")));
    }

    @Test
    void c08_editUnknownCourseRedirectsWithError() throws Exception {
        when(courses.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/courses/99/edit").session(staff()))
                .andExpect(redirectedUrl("/courses"))
                .andExpect(flash().attribute("errorMessage", "Course record was not found."));
    }

    @Test
    void c09_updateCannotLowerCapacityBelowCurrentEnrollments() throws Exception {
        when(courses.findById(1)).thenReturn(Optional.of(course(1, "Algorithms", 30)));
        when(enrollments.countByCourse_CourseId(1)).thenReturn(10L);

        mvc.perform(post("/courses/1").session(staff())
                        .param("courseName", "Algorithms")
                        .param("instructorName", "Lan Pham")
                        .param("capacity", "9"))
                .andExpect(view().name("course-form"))
                .andExpect(model().attributeHasFieldErrorCode("courseForm", "capacity", "belowEnrollmentCount"));

        verify(courses, never()).save(any());
    }

    @Test
    void c10_updateCapacityEqualToEnrollmentsIsAllowed() throws Exception {
        Course stored = course(1, "Algorithms", 30);
        when(courses.findById(1)).thenReturn(Optional.of(stored));
        when(enrollments.countByCourse_CourseId(1)).thenReturn(10L);

        mvc.perform(post("/courses/1").session(staff())
                        .param("courseName", "Algorithms 2")
                        .param("instructorName", "Lan Pham")
                        .param("capacity", "10"))
                .andExpect(redirectedUrl("/courses"))
                .andExpect(flash().attribute("successMessage", "Course record updated."));

        verify(courses).save(stored);
        assertEquals("Algorithms 2", stored.getCourseName());
        assertEquals(10, stored.getCapacity());
    }

    @Test
    void c11_deleteIsBlockedWhileCourseHasEnrollments() throws Exception {
        when(courses.findById(1)).thenReturn(Optional.of(course(1, "Algorithms", 30)));
        when(enrollments.existsByCourse_CourseId(1)).thenReturn(true);

        mvc.perform(post("/courses/1/delete").session(staff()))
                .andExpect(redirectedUrl("/courses"))
                .andExpect(flash().attribute("errorMessage",
                        "Delete this course's enrollment records before deleting the course."));

        verify(courses, never()).delete(any());
    }

    @Test
    void c12_deleteCourseWithoutEnrollments() throws Exception {
        Course stored = course(1, "Algorithms", 30);
        when(courses.findById(1)).thenReturn(Optional.of(stored));

        mvc.perform(post("/courses/1/delete").session(staff()))
                .andExpect(redirectedUrl("/courses"))
                .andExpect(flash().attribute("successMessage", "Course record deleted."));

        verify(courses).delete(stored);
    }

    @Test
    void c13_deleteUnknownCourseShowsError() throws Exception {
        when(courses.findById(99)).thenReturn(Optional.empty());

        mvc.perform(post("/courses/99/delete").session(staff()))
                .andExpect(redirectedUrl("/courses"))
                .andExpect(flash().attribute("errorMessage", "Course record was not found."));
    }

    @Test
    void c14_studentCannotOpenCourseRecords() throws Exception {
        mvc.perform(get("/courses").session(student())).andExpect(status().isForbidden());
        mvc.perform(post("/courses/1/delete").session(student())).andExpect(status().isForbidden());

        verify(courses, never()).delete(any());
    }
}
