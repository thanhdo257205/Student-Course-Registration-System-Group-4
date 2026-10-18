package java03.group4;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.data.domain.Page.empty;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import java03.group4.entity.User;
import java03.group4.repository.UserRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({HomeController.class, StudentController.class, CourseController.class, EnrollmentController.class})
class RecordPagesTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecordStudentRepository studentRepository;

    @MockBean
    private RecordCourseRepository courseRepository;

    @MockBean
    private RecordEnrollmentRepository enrollmentRepository;

    @MockBean
    private UserRepository userRepository;

    private MockHttpSession staffSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", new User("admin", "admin123", "ADMIN"));
        return session;
    }

    @Test
    void dashboardLinksToSeparateRecordLists() throws Exception {
        mockMvc.perform(get("/").session(staffSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("href=\"/students\"")))
                .andExpect(content().string(containsString("href=\"/courses\"")))
                .andExpect(content().string(containsString("href=\"/enrollments\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("No student records yet."))));
    }

    @Test
    void studentListDisplaysPaginationForLargeRecordSets() throws Exception {
        when(studentRepository.search(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), of(1, 10), 25));

        mockMvc.perform(get("/students").session(staffSession()).param("page", "1").param("q", " Minh "))
                .andExpect(status().isOk())
                .andExpect(view().name("students"))
                .andExpect(content().string(containsString("Page 2 of 3")))
                .andExpect(content().string(containsString("href=\"/students?page=0&amp;q=Minh\"")))
                .andExpect(content().string(containsString("href=\"/students?page=2&amp;q=Minh\"")))
                .andExpect(content().string(containsString("value=\"Minh\"")));
    }

    @Test
    void courseAndEnrollmentListsRenderEmptyStates() throws Exception {
        when(courseRepository.search(anyString(), any(Pageable.class))).thenReturn(empty());
        when(enrollmentRepository.search(anyString(), any(Pageable.class))).thenReturn(empty());

        mockMvc.perform(get("/courses").session(staffSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("courses"))
                .andExpect(content().string(containsString("No course records yet.")));

        mockMvc.perform(get("/enrollments").session(staffSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("enrollments"))
                .andExpect(content().string(containsString("No enrollment records yet.")));
    }

    @Test
    void courseAndEnrollmentSearchPassesNormalizedQueryToRepositories() throws Exception {
        when(courseRepository.search(anyString(), any(Pageable.class))).thenReturn(empty());
        when(enrollmentRepository.search(anyString(), any(Pageable.class))).thenReturn(empty());

        mockMvc.perform(get("/courses").session(staffSession()).param("q", " Lan "))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No courses match your search.")));

        mockMvc.perform(get("/enrollments").session(staffSession()).param("q", " Algorithms "))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No enrollments match your search.")));

        verify(courseRepository).search(eq("Lan"), any(Pageable.class));
        verify(enrollmentRepository).search(eq("Algorithms"), any(Pageable.class));
    }

    @Test
    void enrollmentEditFormShowsStoredEnrollmentDate() throws Exception {
        Student student = new Student("Minh Nguyen", "minh@example.edu", LocalDate.of(2004, 5, 12));
        Course course = new Course("Algorithms", "Lan Pham", 30);
        Enrollment enrollment = new Enrollment(student, course, LocalDate.of(2026, 3, 15));
        ReflectionTestUtils.setField(student, "studentId", 1);
        ReflectionTestUtils.setField(course, "courseId", 2);
        ReflectionTestUtils.setField(enrollment, "enrollmentId", 3);

        when(enrollmentRepository.findById(3)).thenReturn(java.util.Optional.of(enrollment));
        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(courseRepository.findAll()).thenReturn(List.of(course));

        mockMvc.perform(get("/enrollments/3/edit").session(staffSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("enrollment-form"))
                .andExpect(content().string(containsString("name=\"enrollmentDate\"")))
                .andExpect(content().string(containsString("value=\"2026-03-15\"")));
    }

    @Test
    void studentEditFormShowsStoredDateOfBirth() throws Exception {
        Student student = new Student("Minh Nguyen", "minh@example.edu", LocalDate.of(2004, 5, 12));
        ReflectionTestUtils.setField(student, "studentId", 1);
        when(studentRepository.findById(1)).thenReturn(java.util.Optional.of(student));

        mockMvc.perform(get("/students/1/edit").session(staffSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("student-form"))
                .andExpect(content().string(containsString("name=\"dateOfBirth\"")))
                .andExpect(content().string(containsString("value=\"2004-05-12\"")));
    }

    @Test
    void studentAndCourseFormsRender() throws Exception {
        mockMvc.perform(get("/students/new").session(staffSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("student-form"))
                .andExpect(content().string(containsString("Save student")));

        mockMvc.perform(get("/courses/new").session(staffSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("course-form"))
                .andExpect(content().string(containsString("Save course")));
    }
}
