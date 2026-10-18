package java03.group4;

import jakarta.validation.Valid;
import java03.group4.repository.UserRepository;
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
public class StudentController {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public StudentController(StudentRepository studentRepository, EnrollmentRepository enrollmentRepository,
                             UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/students")
    public String listStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String q,
            Model model) {
        String query = q.trim();
        model.addAttribute(
                "records",
                RecordPageSupport.load(
                        pageable -> studentRepository.search(query, pageable),
                        page,
                        Sort.by("fullName").ascending().and(Sort.by("studentId").ascending())));
        model.addAttribute("query", query);
        return "students";
    }

    @GetMapping("/students/new")
    public String newStudent(Model model) {
        model.addAttribute("studentForm", new StudentForm());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("formTitle", "Add student");
        model.addAttribute("formAction", "/students");
        return "student-form";
    }

    @PostMapping("/students")
    public String createStudent(
            @Valid @ModelAttribute("studentForm") StudentForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (form.getEmail() != null && studentRepository.existsByEmailIgnoreCase(form.getEmail().trim())) {
            bindingResult.rejectValue("email", "duplicate", "A student with this email already exists.");
        }
        if (bindingResult.hasErrors()) {
            prepareForm(model, "Add student", "/students");
            return "student-form";
        }

        studentRepository.save(new Student(form.getFullName().trim(), form.getEmail().trim(), form.getDateOfBirth()));
        redirectAttributes.addFlashAttribute("successMessage", "Student record created.");
        return "redirect:/students";
    }

    @GetMapping("/students/{studentId}/edit")
    public String editStudent(@PathVariable Integer studentId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Student record was not found.");
            return "redirect:/students";
        }

        Student record = student.get();
        model.addAttribute("studentForm", new StudentForm(record.getFullName(), record.getEmail(), record.getDateOfBirth()));
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("formTitle", "Edit student");
        model.addAttribute("formAction", "/students/" + studentId);
        return "student-form";
    }

    @PostMapping("/students/{studentId}")
    public String updateStudent(
            @PathVariable Integer studentId,
            @Valid @ModelAttribute("studentForm") StudentForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Student record was not found.");
            return "redirect:/students";
        }
        if (form.getEmail() != null
                && studentRepository.existsByEmailIgnoreCaseAndStudentIdNot(form.getEmail().trim(), studentId)) {
            bindingResult.rejectValue("email", "duplicate", "A student with this email already exists.");
        }
        if (bindingResult.hasErrors()) {
            prepareForm(model, "Edit student", "/students/" + studentId);
            return "student-form";
        }

        Student record = student.get();
        record.setFullName(form.getFullName().trim());
        record.setEmail(form.getEmail().trim());
        record.setDateOfBirth(form.getDateOfBirth());
        studentRepository.save(record);
        redirectAttributes.addFlashAttribute("successMessage", "Student record updated.");
        return "redirect:/students";
    }

    @PostMapping("/students/{studentId}/delete")
    public String deleteStudent(@PathVariable Integer studentId, RedirectAttributes redirectAttributes) {
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Student record was not found.");
        } else if (enrollmentRepository.existsByStudent_StudentId(studentId)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Delete this student's enrollment records before deleting the student.");
        } else if (userRepository.existsByStudentId(studentId)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Unlink this student's account before deleting the student.");
        } else {
            studentRepository.delete(student.get());
            redirectAttributes.addFlashAttribute("successMessage", "Student record deleted.");
        }
        return "redirect:/students";
    }

    private void prepareForm(Model model, String title, String action) {
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("today", LocalDate.now());
    }
}
