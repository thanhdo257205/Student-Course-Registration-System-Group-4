package java03.group4.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SessionAccessConfiguration implements WebMvcConfigurer {

    private final SessionAccessInterceptor sessionAccessInterceptor;

    public SessionAccessConfiguration(SessionAccessInterceptor sessionAccessInterceptor) {
        this.sessionAccessInterceptor = sessionAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionAccessInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/css/**",
                        "/images/**",
                        "/js/**",
                        "/webjars/**",
                        "/error",
                        "/favicon.ico",
                        "/login",
                        "/logout");
    }
}
