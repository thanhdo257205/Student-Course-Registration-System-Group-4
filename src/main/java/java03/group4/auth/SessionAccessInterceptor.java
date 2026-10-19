package java03.group4.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.equals("/")) {
            return true;
        }

        Object currentUser = request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute(AuthRestController.SESSION_USER_KEY);
        if (!(currentUser instanceof User user)) {
            String target = path;
            if (request.getQueryString() != null) {
                target += "?" + request.getQueryString();
            }
            response.sendRedirect(request.getContextPath() + "/login?redirect="
                    + URLEncoder.encode(target, StandardCharsets.UTF_8));
            return false;
        }

        if (isCatalogPath(path) && user.isStudent()) {
            return true;
        }
        if (isRecordPath(path) && user.isStaff()) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }

    private boolean isCatalogPath(String path) {
        return path.equals("/catalog") || path.startsWith("/catalog/");
    }

    private boolean isRecordPath(String path) {
        return isPathOrChild(path, "/students")
                || isPathOrChild(path, "/courses")
                || isPathOrChild(path, "/enrollments");
    }

    private boolean isPathOrChild(String path, String prefix) {
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }
}
