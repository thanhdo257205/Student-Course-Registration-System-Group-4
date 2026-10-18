package java03.group4;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Optional;
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

@Controller
public class EnrollmentController {

    private final RecordStudentRepository studentRepository;
    private final RecordCourseRepository courseRepository;
    private final RecordEnrollmentRepository enrollmentRepository;

    public EnrollmentController(
            RecordStudentRepository studentRepository,
            RecordCourseRepository courseRepository,
            RecordEnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping("/enrollments")
    public String listEnrollments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String q,
            Model model) {
        String query = q.trim();
        model.addAttribute(
                "records",
                RecordPageSupport.load(
                        pageable -> enrollmentRepository.search(query, pageable),
                        page,
                        Sort.by("enrollmentDate").descending().and(Sort.by("enrollmentId").descending())));
        model.addAttribute("query", query);
        return "enrollments";
    }

    @GetMapping("/enrollments/new")
    public String newEnrollment(Model model, RedirectAttributes redirectAttributes) {
        if (!prepareOptions(model)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Add at least one student and one course before creating an enrollment.");
            return "redirect:/enrollments";
        }
        model.addAttribute("enrollmentForm", new EnrollmentForm(null, null, LocalDate.now()));
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("formTitle", "Add enrollment");
        model.addAttribute("formAction", "/enrollments");
        return "enrollment-form";
    }

    @PostMapping("/enrollments")
    public String createEnrollment(
            @Valid @ModelAttribute("enrollmentForm") EnrollmentForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        Optional<Student> student = form.getStudentId() == null
                ? Optional.empty()
                : studentRepository.findById(form.getStudentId());
        Optional<Course> course = form.getCourseId() == null
                ? Optional.empty()
                : courseRepository.findById(form.getCourseId());
        validateEnrollment(form, bindingResult, student, course, null);
        if (bindingResult.hasErrors()) {
            prepareOptions(model);
            prepareForm(model, "Add enrollment", "/enrollments");
            return "enrollment-form";
        }

        enrollmentRepository.save(new Enrollment(student.get(), course.get(), form.getEnrollmentDate()));
        redirectAttributes.addFlashAttribute("successMessage", "Enrollment record created.");
        return "redirect:/enrollments";
    }

    @GetMapping("/enrollments/{enrollmentId}/edit")
    public String editEnrollment(
            @PathVariable Integer enrollmentId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Enrollment> enrollment = enrollmentRepository.findById(enrollmentId);
        if (enrollment.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Enrollment record was not found.");
            return "redirect:/enrollments";
        }
        if (!prepareOptions(model)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Add at least one student and one course before editing an enrollment.");
            return "redirect:/enrollments";
        }

        Enrollment record = enrollment.get();
        model.addAttribute(
                "enrollmentForm",
                new EnrollmentForm(
                        record.getStudent().getStudentId(),
                        record.getCourse().getCourseId(),
                        record.getEnrollmentDate()));
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("formTitle", "Edit enrollment");
        model.addAttribute("formAction", "/enrollments/" + enrollmentId);
        return "enrollment-form";
    }

    @PostMapping("/enrollments/{enrollmentId}")
    public String updateEnrollment(
            @PathVariable Integer enrollmentId,
            @Valid @ModelAttribute("enrollmentForm") EnrollmentForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        Optional<Enrollment> enrollment = enrollmentRepository.findById(enrollmentId);
        if (enrollment.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Enrollment record was not found.");
            return "redirect:/enrollments";
        }

        Optional<Student> student = form.getStudentId() == null
                ? Optional.empty()
                : studentRepository.findById(form.getStudentId());
        Optional<Course> course = form.getCourseId() == null
                ? Optional.empty()
                : courseRepository.findById(form.getCourseId());
        validateEnrollment(form, bindingResult, student, course, enrollment.get());
        if (bindingResult.hasErrors()) {
            prepareOptions(model);
            prepareForm(model, "Edit enrollment", "/enrollments/" + enrollmentId);
            return "enrollment-form";
        }

        Enrollment record = enrollment.get();
        record.setStudent(student.get());
        record.setCourse(course.get());
        record.setEnrollmentDate(form.getEnrollmentDate());
        enrollmentRepository.save(record);
        redirectAttributes.addFlashAttribute("successMessage", "Enrollment record updated.");
        return "redirect:/enrollments";
    }

    @PostMapping("/enrollments/{enrollmentId}/delete")
    public String deleteEnrollment(@PathVariable Integer enrollmentId, RedirectAttributes redirectAttributes) {
        Optional<Enrollment> enrollment = enrollmentRepository.findById(enrollmentId);
        if (enrollment.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Enrollment record was not found.");
        } else {
            enrollmentRepository.delete(enrollment.get());
            redirectAttributes.addFlashAttribute("successMessage", "Enrollment record deleted.");
        }
        return "redirect:/enrollments";
    }

    private void validateEnrollment(
            EnrollmentForm form,
            BindingResult bindingResult,
            Optional<Student> student,
            Optional<Course> course,
            Enrollment currentEnrollment) {
        if (form.getStudentId() != null && student.isEmpty()) {
            bindingResult.rejectValue("studentId", "notFound", "Choose an existing student.");
        }
        if (form.getCourseId() != null && course.isEmpty()) {
            bindingResult.rejectValue("courseId", "notFound", "Choose an existing course.");
        }
        if (student.isEmpty() || course.isEmpty()) {
            return;
        }

        Integer currentEnrollmentId = currentEnrollment == null ? null : currentEnrollment.getEnrollmentId();
        boolean duplicate = currentEnrollmentId == null
                ? enrollmentRepository.existsByStudent_StudentIdAndCourse_CourseId(
                        form.getStudentId(), form.getCourseId())
                : enrollmentRepository.existsByStudent_StudentIdAndCourse_CourseIdAndEnrollmentIdNot(
                        form.getStudentId(), form.getCourseId(), currentEnrollmentId);
        if (duplicate) {
            bindingResult.reject("duplicate", "This student is already enrolled in this course.");
        }

        long enrollmentCount = enrollmentRepository.countByCourse_CourseId(form.getCourseId());
        boolean movingToCourse = currentEnrollment == null
                || !currentEnrollment.getCourse().getCourseId().equals(form.getCourseId());
        if (movingToCourse && enrollmentCount >= course.get().getCapacity()) {
            bindingResult.rejectValue("courseId", "full", "This course has reached its capacity.");
        }
    }

    private boolean prepareOptions(Model model) {
        var students = studentRepository.findAll();
        var courses = courseRepository.findAll();
        model.addAttribute("students", students);
        model.addAttribute("courses", courses);
        if (students.isEmpty() || courses.isEmpty()) {
            model.addAttribute("optionMessage", "Add at least one student and one course before creating an enrollment.");
            return false;
        }
        return true;
    }

    private void prepareForm(Model model, String title, String action) {
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("today", LocalDate.now());
    }
}
