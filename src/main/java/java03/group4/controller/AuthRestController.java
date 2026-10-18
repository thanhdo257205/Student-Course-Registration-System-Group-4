package java03.group4.controller;

import jakarta.servlet.http.HttpSession;
import java03.group4.dto.LoginRequest;
import java03.group4.dto.LoginResponse;
import java03.group4.entity.User;
import java03.group4.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    public static final String SESSION_USER_KEY = "currentUser";

    private final AuthService authService;

    public AuthRestController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * RESTful API: Sign in with credentials.
     * Consumes JSON: { "username": "admin", "password": "...", "rememberMe": false }
     * Returns JSON: { success, message, username, role, redirectUrl }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody(required = false) LoginRequest request, HttpSession session) {
        if (request == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(LoginResponse.fail("Invalid request payload."));
        }

        try {
            User user = authService.authenticate(request.getUsername(), request.getPassword());
            session.setAttribute(SESSION_USER_KEY, user);
            
            // Extend session lifetime if rememberMe is requested
            if (request.isRememberMe()) {
                session.setMaxInactiveInterval(7 * 24 * 60 * 60); // 7 days
            } else {
                session.setMaxInactiveInterval(30 * 60); // 30 minutes
            }

            return ResponseEntity.ok(
                    LoginResponse.ok("Signed in successfully.", user.getUsername(), user.getRole(), user.getLandingPath())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.fail(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.fail("Internal authentication error. Please try again."));
        }
    }

    /**
     * RESTful API: Sign out and invalidate current session.
     */
    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(HttpSession session) {
        session.removeAttribute(SESSION_USER_KEY);
        session.invalidate();
        return ResponseEntity.ok(LoginResponse.ok("Signed out successfully.", null, null, "/login"));
    }

    /**
     * RESTful API: Retrieve current authenticated user info.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Object userObj = session.getAttribute(SESSION_USER_KEY);
        if (userObj instanceof User user) {
            Map<String, Object> data = new HashMap<>();
            data.put("authenticated", true);
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("role", user.getRole());
            return ResponseEntity.ok(data);
        }

        Map<String, Object> anonymous = new HashMap<>();
        anonymous.put("authenticated", false);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(anonymous);
    }
}
