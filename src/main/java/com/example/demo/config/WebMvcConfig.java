package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC configuration.
 * <p>
 * Serves uploaded photos that are stored in {@code uploads/photos/} under the URL
 * path {@code /uploads/photos/**} so that Thymeleaf templates can reference them
 * directly as {@code <img th:src="@{/uploads/photos/{fn}(fn=${profile.photoFileName})}">}.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded photos
        String uploadPath = Paths.get("uploads/photos").toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/photos/**")
                .addResourceLocations(uploadPath);

        // Serve static assets from classpath (CSS, JS, images)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
