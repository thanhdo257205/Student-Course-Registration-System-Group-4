package java03.group4.enrollment;

import jakarta.servlet.http.HttpSession;
import java03.group4.auth.AuthRestController;
import java03.group4.auth.User;
import java03.group4.course.Course;
import java03.group4.course.CourseRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/catalog")
public class CatalogController {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;

    public CatalogController(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository,
                             EnrollmentService enrollmentService) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public String browse(@RequestParam(defaultValue = "") String q, HttpSession session, Model model) {
        String keyword = q.trim();
        User user = (User) session.getAttribute(AuthRestController.SESSION_USER_KEY);
        model.addAttribute("q", keyword);
        model.addAttribute("courses", courseRepository
                .findByCourseNameContainingIgnoreCaseOrInstructorNameContainingIgnoreCaseOrderByCourseName(keyword, keyword));
        model.addAttribute("counts", enrollmentService.enrolledCounts());
        model.addAttribute("registeredCourseIds", user.getStudentId() == null
                ? Set.of()
                : Set.copyOf(enrollmentRepository.findCourseIdsByStudentId(user.getStudentId())));
        return "catalog";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, HttpSession session, Model model) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<Enrollment> enrollments = enrollmentRepository.findByCourse_CourseIdOrderByEnrollmentDateDescEnrollmentIdDesc(id);
        User user = (User) session.getAttribute(AuthRestController.SESSION_USER_KEY);
        model.addAttribute("course", course);
        model.addAttribute("enrollmentCount", enrollments.size());
        model.addAttribute("seatsLeft", Math.max(0, course.getCapacity() - enrollments.size()));
        model.addAttribute("studentLinked", user.getStudentId() != null);
        model.addAttribute("alreadyEnrolled", user.getStudentId() != null
                && enrollments.stream().anyMatch(enrollment ->
                        user.getStudentId().equals(enrollment.getStudent().getStudentId())));
        return "course";
    }

    @PostMapping("/{id}/register")
    public String register(@PathVariable Integer id, HttpSession session, RedirectAttributes redirect) {
        User user = (User) session.getAttribute(AuthRestController.SESSION_USER_KEY);
        try {
            if (user.getStudentId() == null) {
                throw new IllegalArgumentException("This student account is not linked to a student record.");
            }
            enrollmentService.register(user.getStudentId(), id);
            redirect.addFlashAttribute("message", "Đăng ký thành công.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            // uq_student_course chan ban ghi trung khi 2 request dang ky lot qua check exists cung luc
            redirect.addFlashAttribute("error", "Sinh viên đã đăng ký khóa học này.");
        }
        return "redirect:/catalog/" + id;
    }
}
