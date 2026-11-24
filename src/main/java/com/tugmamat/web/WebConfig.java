package com.tugmamat.web;

import com.tugmamat.repo.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserRepository users;

    public WebConfig(UserRepository users) { this.users = users; }

    /** Login is required for every page except those listed below. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
                HttpSession s = req.getSession(false);
                Long id = s == null ? null : (Long) s.getAttribute("userId");
                // If the user no longer exists in the database (e.g. data was reset), log out the stale session
                if (id == null || !users.existsById(id)) {
                    if (s != null) s.invalidate();
                    res.sendRedirect("/login");
                    return false;
                }
                return true;
            }
        }).addPathPatterns("/**")
          .excludePathPatterns("/", "/login", "/register", "/css/**", "/js/**", "/img/**", "/uploads/**", "/h2-console/**", "/error", "/favicon.ico");
    }

    /** Serves uploaded images from ./uploads in the browser */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:uploads/");
    }
}
