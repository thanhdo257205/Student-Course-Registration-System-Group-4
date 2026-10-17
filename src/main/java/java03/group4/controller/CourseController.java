package java03.group4.controller;

import java03.group4.entity.Course;
import java03.group4.entity.Enrollment;
import java03.group4.repository.CourseRepository;
import java03.group4.repository.EnrollmentRepository;
import java03.group4.repository.StudentRepository;
import java03.group4.service.EnrollmentService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;

    public CourseController(CourseRepository courseRepository, StudentRepository studentRepository,
                            EnrollmentRepository enrollmentRepository, EnrollmentService enrollmentService) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public String browse(@RequestParam(defaultValue = "") String q, Model model) {
        String keyword = q.trim();
        model.addAttribute("q", keyword);
        model.addAttribute("courses", courseRepository
                .findByCourseNameContainingIgnoreCaseOrInstructorNameContainingIgnoreCaseOrderByCourseName(keyword, keyword));
        model.addAttribute("counts", enrollmentService.enrolledCounts());
        return "courses";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<Enrollment> enrollments = enrollmentRepository.findByCourse_IdOrderByEnrollmentDateDescIdDesc(id);
        Set<Integer> enrolledIds = enrollments.stream().map(e -> e.getStudent().getId()).collect(Collectors.toSet());
        model.addAttribute("course", course);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("seatsLeft", Math.max(0, course.getCapacity() - enrollments.size()));
        // Chi cho chon sinh vien chua dang ky khoa nay
        model.addAttribute("students", studentRepository.findAll(Sort.by("fullName")).stream()
                .filter(s -> !enrolledIds.contains(s.getId())).toList());
        return "course";
    }

    @PostMapping("/{id}/register")
    public String register(@PathVariable Integer id, @RequestParam Integer studentId, RedirectAttributes redirect) {
        try {
            enrollmentService.register(studentId, id);
            redirect.addFlashAttribute("message", "Đăng ký thành công.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/courses/" + id;
    }
}
