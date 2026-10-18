package java03.group4;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller("recordsCourseController")
public class CourseController {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CourseController(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping("/courses")
    public String listCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String q,
            Model model) {
        String query = q.trim();
        var courses = RecordPageSupport.load(
                pageable -> courseRepository.search(query, pageable),
                page,
                Sort.by("courseName").ascending().and(Sort.by("courseId").ascending()));
        Map<Integer, Long> enrollmentCounts = courses.isEmpty()
                ? Map.of()
                : enrollmentRepository.countPerCourse(
                                courses.getContent().stream().map(Course::getCourseId).toList())
                        .stream()
                        .collect(Collectors.toMap(
                                CourseEnrollmentSummary::getCourseId,
                                CourseEnrollmentSummary::getEnrollmentCount));
        model.addAttribute("records", courses);
        model.addAttribute("enrollmentCounts", enrollmentCounts);
        model.addAttribute("query", query);
        return "courses";
    }

    @GetMapping("/courses/new")
    public String newCourse(Model model) {
        model.addAttribute("courseForm", new CourseForm());
        model.addAttribute("formTitle", "Add course");
        model.addAttribute("formAction", "/courses");
        return "course-form";
    }

    @PostMapping("/courses")
    public String createCourse(
            @Valid @ModelAttribute("courseForm") CourseForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, "Add course", "/courses");
            return "course-form";
        }

        courseRepository.save(new Course(form.getCourseName().trim(), form.getInstructorName().trim(), form.getCapacity()));
        redirectAttributes.addFlashAttribute("successMessage", "Course record created.");
        return "redirect:/courses";
    }

    @GetMapping("/courses/{courseId}/edit")
    public String editCourse(@PathVariable Integer courseId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Course> course = courseRepository.findById(courseId);
        if (course.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course record was not found.");
            return "redirect:/courses";
        }

        Course record = course.get();
        model.addAttribute("courseForm", new CourseForm(record.getCourseName(), record.getInstructorName(), record.getCapacity()));
        model.addAttribute("formTitle", "Edit course");
        model.addAttribute("formAction", "/courses/" + courseId);
        return "course-form";
    }

    @PostMapping("/courses/{courseId}")
    public String updateCourse(
            @PathVariable Integer courseId,
            @Valid @ModelAttribute("courseForm") CourseForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        Optional<Course> course = courseRepository.findById(courseId);
        if (course.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course record was not found.");
            return "redirect:/courses";
        }
        long enrollmentCount = enrollmentRepository.countByCourse_CourseId(courseId);
        if (form.getCapacity() != null && form.getCapacity() < enrollmentCount) {
            bindingResult.rejectValue(
                    "capacity", "belowEnrollmentCount", "Capacity cannot be lower than the current enrollment count.");
        }
        if (bindingResult.hasErrors()) {
            prepareForm(model, "Edit course", "/courses/" + courseId);
            return "course-form";
        }

        Course record = course.get();
        record.setCourseName(form.getCourseName().trim());
        record.setInstructorName(form.getInstructorName().trim());
        record.setCapacity(form.getCapacity());
        courseRepository.save(record);
        redirectAttributes.addFlashAttribute("successMessage", "Course record updated.");
        return "redirect:/courses";
    }

    @PostMapping("/courses/{courseId}/delete")
    public String deleteCourse(@PathVariable Integer courseId, RedirectAttributes redirectAttributes) {
        Optional<Course> course = courseRepository.findById(courseId);
        if (course.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course record was not found.");
        } else if (enrollmentRepository.existsByCourse_CourseId(courseId)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Delete this course's enrollment records before deleting the course.");
        } else {
            courseRepository.delete(course.get());
            redirectAttributes.addFlashAttribute("successMessage", "Course record deleted.");
        }
        return "redirect:/courses";
    }

    private void prepareForm(Model model, String title, String action) {
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
    }
}
