package java03.group4.enrollment;

import java03.group4.auth.User;
import java03.group4.course.Course;
import java03.group4.course.CourseRepository;
import java03.group4.student.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CatalogController.class)
class CatalogControllerTest {

    @Autowired private MockMvc mvc;
    @MockBean private CourseRepository courses;
    @MockBean private EnrollmentRepository enrollments;
    @MockBean private EnrollmentService enrollmentService;

    private MockHttpSession studentSession() {
        User user = new User("student", "student123", "STUDENT");
        user.setStudentId(7);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);
        return session;
    }

    private Course javaCourse() {
        Course course = new Course("Lập trình Java", "Nguyễn Thị Mai", 3);
        ReflectionTestUtils.setField(course, "courseId", 1);
        return course;
    }

    @Test
    void detailShowsCourseAndRegistrationForCurrentStudent() throws Exception {
        Course course = javaCourse();
        Student enrolledStudent = new Student("Minh Nguyen", "minh@example.edu", LocalDate.of(2004, 5, 12));
        ReflectionTestUtils.setField(enrolledStudent, "studentId", 7);
        Enrollment e = new Enrollment(enrolledStudent, course, LocalDate.now());
        when(courses.findById(1)).thenReturn(Optional.of(course));
        when(enrollments.findByCourse_CourseIdOrderByEnrollmentDateDescEnrollmentIdDesc(1)).thenReturn(List.of(e));

        mvc.perform(get("/catalog/1").session(studentSession()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("seatsLeft", 2))
                .andExpect(model().attribute("alreadyEnrolled", true))
                .andExpect(content().string(containsString("already registered")))
                .andExpect(content().string(not(containsString("studentId"))));
    }

    @Test
    void unknownCourseIs404() throws Exception {
        when(courses.findById(99)).thenReturn(Optional.empty());
        mvc.perform(get("/catalog/99").session(studentSession())).andExpect(status().isNotFound());
    }

    @Test
    void browseLinksToDetail() throws Exception {
        when(courses.findByCourseNameContainingIgnoreCaseOrInstructorNameContainingIgnoreCaseOrderByCourseName(anyString(), anyString()))
                .thenReturn(List.of(javaCourse()));
        when(enrollmentService.enrolledCounts()).thenReturn(Map.of());

        mvc.perform(get("/catalog").session(studentSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/catalog/1\"")))
                .andExpect(content().string(containsString("href=\"/catalog/1#register\"")));
    }

    @Test
    void browseShowsRegisteredInsteadOfRegisterButtonForOwnCourses() throws Exception {
        when(courses.findByCourseNameContainingIgnoreCaseOrInstructorNameContainingIgnoreCaseOrderByCourseName(anyString(), anyString()))
                .thenReturn(List.of(javaCourse()));
        when(enrollmentService.enrolledCounts()).thenReturn(Map.of(1, 1L));
        when(enrollments.findCourseIdsByStudentId(7)).thenReturn(List.of(1));

        mvc.perform(get("/catalog").session(studentSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registered")))
                .andExpect(content().string(not(containsString("href=\"/catalog/1#register\""))))
                // header chung voi trang admin
                .andExpect(content().string(containsString("class=\"site-header\"")))
                .andExpect(content().string(containsString("student (STUDENT)")));
    }

    @Test
    void registrationUsesTheStudentFromTheSession() throws Exception {
        mvc.perform(post("/catalog/1/register").session(studentSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/1"));

        org.mockito.Mockito.verify(enrollmentService).register(7, 1);
    }

    @Test
    void concurrentDuplicateRegistrationShowsErrorInsteadOf500() throws Exception {
        org.mockito.Mockito.doThrow(new org.springframework.dao.DataIntegrityViolationException("uq_student_course"))
                .when(enrollmentService).register(7, 1);

        mvc.perform(post("/catalog/1/register").session(studentSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/1"))
                .andExpect(flash().attribute("error", "Sinh viên đã đăng ký khóa học này."));
    }
}
