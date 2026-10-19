package java03.group4.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        Object currentUser = session.getAttribute(AuthRestController.SESSION_USER_KEY);
        if (!(currentUser instanceof User user)) {
            return "redirect:/login";
        }
        return user.isStudent() ? "redirect:/catalog" : "index";
    }
}
