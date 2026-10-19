package java03.group4;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java03.group4.course.CourseForm;
import java03.group4.enrollment.EnrollmentForm;
import java03.group4.student.StudentForm;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class RecordFormValidationTests {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private final Validator validator = VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void studentRequiresValidDetailsAndPastDateOfBirth() {
        StudentForm form = new StudentForm(" ", "not-an-email", LocalDate.now().plusDays(1));

        var violations = validator.validate(form);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fullName")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")));
    }

    @Test
    void courseCapacityMustBePositive() {
        CourseForm form = new CourseForm("Algorithms", "Instructor", 0);

        var violations = validator.validate(form);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("capacity")));
    }

    @Test
    void enrollmentDateCannotBeInTheFuture() {
        EnrollmentForm form = new EnrollmentForm(1, 1, LocalDate.now().plusDays(1));

        var violations = validator.validate(form);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("enrollmentDate")));
    }
}
