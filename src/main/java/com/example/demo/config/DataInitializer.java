package com.example.demo.config;

import com.example.demo.model.Template;
import com.example.demo.repository.TemplateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seeds the database with default ID-card templates on first startup.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedTemplates(TemplateRepository templateRepository) {
        return args -> {
            if (templateRepository.count() == 0) {
                templateRepository.save(Template.builder()
                        .code("CLASSIC_BLUE")
                        .name("Classic Blue")
                        .organizationName("ITC Institution")
                        .layout("VERTICAL")
                        .primaryColor("#1d4ed8")
                        .secondaryColor("#dbeafe")
                        .textColor("#1e3a5f")
                        .tagline("Excellence in Education")
                        .active(true)
                        .build());

                templateRepository.save(Template.builder()
                        .code("MODERN_DARK")
                        .name("Modern Dark")
                        .organizationName("ITC Institution")
                        .layout("VERTICAL")
                        .primaryColor("#111827")
                        .secondaryColor("#374151")
                        .textColor("#f9fafb")
                        .tagline("Innovation & Technology")
                        .active(true)
                        .build());

                templateRepository.save(Template.builder()
                        .code("STUDENT_GREEN")
                        .name("Student Green")
                        .organizationName("ITC Institution")
                        .layout("VERTICAL")
                        .primaryColor("#059669")
                        .secondaryColor("#d1fae5")
                        .textColor("#064e3b")
                        .tagline("Learning for Tomorrow")
                        .active(true)
                        .build());

                templateRepository.save(Template.builder()
                        .code("ROYAL_PURPLE")
                        .name("Royal Purple")
                        .organizationName("ITC Institution")
                        .layout("VERTICAL")
                        .primaryColor("#7c3aed")
                        .secondaryColor("#ede9fe")
                        .textColor("#3b0764")
                        .tagline("Knowledge is Power")
                        .active(true)
                        .build());

                System.out.println("[DataInitializer] Seeded 4 default templates.");
            }
        };
    }
}
