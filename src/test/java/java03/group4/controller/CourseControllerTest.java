package java03.group4.controller;

import java03.group4.entity.Course;
import java03.group4.entity.Enrollment;
import java03.group4.entity.Student;
import java03.group4.repository.CourseRepository;
import java03.group4.repository.EnrollmentRepository;
import java03.group4.repository.StudentRepository;
import java03.group4.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired private MockMvc mvc;
    @MockBean private CourseRepository courses;
    @MockBean private StudentRepository students;
    @MockBean private EnrollmentRepository enrollments;
    @MockBean private EnrollmentService enrollmentService;

    private static Student student(int id, String name) {
        Student s = new Student();
        s.setId(id);
        s.setFullName(name);
        s.setEmail("s" + id + "@student.edu.vn");
        return s;
    }

    @Test
    void detailShowsRosterAndOnlyUnenrolledStudents() throws Exception {
        Course course = new Course();
        course.setId(1);
        course.setCourseName("Lập trình Java");
        course.setInstructorName("Nguyễn Thị Mai");
        course.setCapacity(3);
        Enrollment e = new Enrollment();
        e.setCourse(course);
        e.setStudent(student(7, "Trần Thị Bình"));
        when(courses.findById(1)).thenReturn(Optional.of(course));
        when(enrollments.findByCourse_IdOrderByEnrollmentDateDescIdDesc(1)).thenReturn(List.of(e));
        when(students.findAll(any(Sort.class))).thenReturn(List.of(student(7, "Trần Thị Bình"), student(8, "Lê Hoàng Cường")));

        mvc.perform(get("/courses/1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("seatsLeft", 2))
                .andExpect(model().attribute("students", hasSize(1)))
                .andExpect(content().string(containsString("Trần Thị Bình")))
                .andExpect(content().string(containsString("option value=\"8\"")))
                .andExpect(content().string(not(containsString("option value=\"7\""))));
    }

    @Test
    void unknownCourseIs404() throws Exception {
        when(courses.findById(99)).thenReturn(Optional.empty());
        mvc.perform(get("/courses/99")).andExpect(status().isNotFound());
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

        mvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/courses/1\"")))
                .andExpect(content().string(containsString("href=\"/courses/1#register\"")));
    }
}
