package java03.group4.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Display login view (Thymeleaf template).
     */
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "redirect", required = false) String redirect,
                                HttpSession session,
                                Model model) {
        Object currentUser = session.getAttribute(AuthRestController.SESSION_USER_KEY);
        if (currentUser instanceof User user) {
            return "redirect:" + user.getLandingPath();
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been safely signed out.");
        }

        if (redirect != null && !redirect.isBlank()) {
            model.addAttribute("redirectUrl", redirect);
        }

        return "login";
    }

    /**
     * Fallback standard form login handler (if JavaScript is disabled).
     */
    @PostMapping("/login")
    public String handleFormLogin(@RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam(value = "rememberMe", defaultValue = "false") boolean rememberMe,
                                 @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                                 HttpSession session,
                                 Model model) {
        try {
            User user = authService.authenticate(username, password);
            session.setAttribute(AuthRestController.SESSION_USER_KEY, user);
            
            if (rememberMe) {
                session.setMaxInactiveInterval(7 * 24 * 60 * 60);
            }

            if (isAllowedRedirect(user, redirectUrl)) {
                return "redirect:" + redirectUrl;
            }
            return "redirect:" + user.getLandingPath();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            return "login";
        }
    }

    /**
     * Sign out and redirect back to login page.
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(AuthRestController.SESSION_USER_KEY);
        session.invalidate();
        return "redirect:/login?logout=true";
    }

    private boolean isAllowedRedirect(User user, String redirectUrl) {
        if (redirectUrl == null || !redirectUrl.startsWith("/") || redirectUrl.startsWith("//")) {
            return false;
        }
        if (user.isStudent()) {
            return redirectUrl.equals("/catalog") || redirectUrl.startsWith("/catalog/");
        }
        return user.isStaff() && (redirectUrl.equals("/students") || redirectUrl.startsWith("/students/")
                || redirectUrl.equals("/courses") || redirectUrl.startsWith("/courses/")
                || redirectUrl.equals("/enrollments") || redirectUrl.startsWith("/enrollments/"));
    }
}
