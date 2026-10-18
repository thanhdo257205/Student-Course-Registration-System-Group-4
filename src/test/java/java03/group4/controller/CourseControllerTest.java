package java03.group4.controller;

import java03.group4.entity.Course;
import java03.group4.entity.Enrollment;
import java03.group4.entity.User;
import java03.group4.repository.CourseRepository;
import java03.group4.repository.EnrollmentRepository;
import java03.group4.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

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

    @Test
    void detailShowsCourseAndRegistrationForCurrentStudent() throws Exception {
        Course course = new Course();
        course.setId(1);
        course.setCourseName("Lập trình Java");
        course.setInstructorName("Nguyễn Thị Mai");
        course.setCapacity(3);
        Enrollment e = new Enrollment();
        e.setCourse(course);
        java03.group4.entity.Student enrolledStudent = new java03.group4.entity.Student();
        enrolledStudent.setId(7);
        e.setStudent(enrolledStudent);
        when(courses.findById(1)).thenReturn(Optional.of(course));
        when(enrollments.findByCourse_IdOrderByEnrollmentDateDescIdDesc(1)).thenReturn(List.of(e));

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
        Course course = new Course();
        course.setId(1);
        course.setCourseName("Lập trình Java");
        course.setInstructorName("Nguyễn Thị Mai");
        course.setCapacity(3);
        when(courses.findByCourseNameContainingIgnoreCaseOrInstructorNameContainingIgnoreCaseOrderByCourseName(anyString(), anyString()))
                .thenReturn(List.of(course));
        when(enrollmentService.enrolledCounts()).thenReturn(Map.of());

        mvc.perform(get("/catalog").session(studentSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/catalog/1\"")))
                .andExpect(content().string(containsString("href=\"/catalog/1#register\"")));
    }

    @Test
    void registrationUsesTheStudentFromTheSession() throws Exception {
        mvc.perform(post("/catalog/1/register").session(studentSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/1"));

        org.mockito.Mockito.verify(enrollmentService).register(7, 1);
    }
}
