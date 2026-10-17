package java03.group4;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Page.empty;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({HomeController.class, StudentController.class, CourseController.class, EnrollmentController.class})
class RecordPagesTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentRepository studentRepository;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private EnrollmentRepository enrollmentRepository;

    @Test
    void dashboardLinksToSeparateRecordLists() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("href=\"/students\"")))
                .andExpect(content().string(containsString("href=\"/courses\"")))
                .andExpect(content().string(containsString("href=\"/enrollments\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("No student records yet."))));
    }

    @Test
    void studentListDisplaysPaginationForLargeRecordSets() throws Exception {
        when(studentRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), of(1, 10), 25));

        mockMvc.perform(get("/students").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("students"))
                .andExpect(content().string(containsString("Page 2 of 3")))
                .andExpect(content().string(containsString("href=\"/students?page=0\"")))
                .andExpect(content().string(containsString("href=\"/students?page=2\"")));
    }

    @Test
    void courseAndEnrollmentListsRenderEmptyStates() throws Exception {
        when(courseRepository.findAll(any(Pageable.class))).thenReturn(empty());
        when(enrollmentRepository.findAll(any(Pageable.class))).thenReturn(empty());

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses"))
                .andExpect(content().string(containsString("No course records yet.")));

        mockMvc.perform(get("/enrollments"))
                .andExpect(status().isOk())
                .andExpect(view().name("enrollments"))
                .andExpect(content().string(containsString("No enrollment records yet.")));
    }

    @Test
    void studentAndCourseFormsRender() throws Exception {
        mockMvc.perform(get("/students/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("student-form"))
                .andExpect(content().string(containsString("Save student")));

        mockMvc.perform(get("/courses/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("course-form"))
                .andExpect(content().string(containsString("Save course")));
    }
}
