package java03.group4.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class StudentForm {

    @NotBlank(message = "Enter the student's full name.")
    @Size(max = 100, message = "Full name must be 100 characters or fewer.")
    private String fullName;

    @NotBlank(message = "Enter an email address.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 100, message = "Email must be 100 characters or fewer.")
    private String email;

    @NotNull(message = "Enter the student's date of birth.")
    @PastOrPresent(message = "Date of birth cannot be in the future.")
    private LocalDate dateOfBirth;

    public StudentForm() {
    }

    public StudentForm(String fullName, String email, LocalDate dateOfBirth) {
        this.fullName = fullName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
