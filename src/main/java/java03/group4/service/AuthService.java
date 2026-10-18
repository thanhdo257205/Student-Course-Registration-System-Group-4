package java03.group4.service;

import java03.group4.entity.User;
import java03.group4.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        return user;
    }

    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username.trim());
    }
}
