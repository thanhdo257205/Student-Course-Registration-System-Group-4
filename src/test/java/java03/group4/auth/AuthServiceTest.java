package java03.group4.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java03.group4.student.Student;
import java03.group4.student.StudentRepository;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final StudentRepository students = mock(StudentRepository.class);
    private final AuthService service = new AuthService(users, students);

    private User studentAccount() {
        User user = new User("student", "student123", "STUDENT");
        user.setStudentId(1);
        when(users.findByUsername("student")).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void activeStudentCanSignIn() {
        User user = studentAccount();
        when(students.findById(1)).thenReturn(Optional.of(
                new Student("Minh Nguyen", "minh@example.edu", LocalDate.of(2004, 3, 15))));

        assertEquals(user, service.authenticate("student", "student123"));
    }

    @Test
    void inactiveStudentCannotSignIn() {
        studentAccount();
        Student inactive = new Student("Minh Nguyen", "minh@example.edu", LocalDate.of(2004, 3, 15));
        inactive.setActive(false);
        when(students.findById(1)).thenReturn(Optional.of(inactive));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.authenticate("student", "student123"));
        assertEquals("This student account is inactive. Contact school staff.", error.getMessage());
    }
}
