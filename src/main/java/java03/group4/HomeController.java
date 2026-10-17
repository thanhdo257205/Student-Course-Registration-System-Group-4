package java03.group4;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public HomeController(
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        var students = studentRepository.findAll();
        var courses = courseRepository.findAll();
        var enrollments = enrollmentRepository.findAll();
        Map<Integer, Long> courseEnrollmentCounts = enrollments.stream()
                .collect(Collectors.groupingBy(enrollment -> enrollment.getCourse().getCourseId(), Collectors.counting()));

        model.addAttribute("students", students);
        model.addAttribute("courses", courses);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("courseEnrollmentCounts", courseEnrollmentCounts);
        return "index";
    }
}
