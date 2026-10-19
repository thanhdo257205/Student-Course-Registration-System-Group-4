package java03.group4.auth;

import java03.group4.student.Student;
import java03.group4.student.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public AuthService(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * Authenticates user credentials with username and password.
     * Throws IllegalArgumentException if credentials are invalid.
     */
    public User authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        String trimmedUsername = username.trim();
        Optional<User> userOpt = userRepository.findByUsername(trimmedUsername);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        User user = userOpt.get();

        // Compare password directly with seed data from docs/database.sql
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        if (!user.isStudent() && !user.isStaff()) {
            throw new IllegalArgumentException("This account does not have an application role.");
        }
        if (user.isStudent() && user.getStudentId() != null
                && !studentRepository.findById(user.getStudentId()).map(Student::isActive).orElse(true)) {
            throw new IllegalArgumentException("This student account is inactive. Contact school staff.");
        }

        return user;
    }

    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username.trim());
    }
}
