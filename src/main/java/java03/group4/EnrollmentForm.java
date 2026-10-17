package java03.group4;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

public class EnrollmentForm {

    @NotNull(message = "Choose a student.")
    private Integer studentId;

    @NotNull(message = "Choose a course.")
    private Integer courseId;

    @NotNull(message = "Enter an enrollment date.")
    @PastOrPresent(message = "Enrollment date cannot be in the future.")
    private LocalDate enrollmentDate;

    public EnrollmentForm() {
    }

    public EnrollmentForm(Integer studentId, Integer courseId, LocalDate enrollmentDate) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.enrollmentDate = enrollmentDate;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
}
