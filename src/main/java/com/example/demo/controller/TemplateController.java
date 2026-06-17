package com.example.demo.controller;

import com.example.demo.model.Template;
import com.example.demo.repository.TemplateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TemplateController {

    private final TemplateRepository templateRepository;

    public TemplateController(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    /**
     * Get all templates.
     */
    @GetMapping
    public ResponseEntity<List<Template>> getAllTemplates() {
        return ResponseEntity.ok(templateRepository.findAll());
    }

    /**
     * Get template by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Template> getTemplateById(@PathVariable Long id) {
        return templateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get template by code.
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Template> getTemplateByCode(@PathVariable String code) {
        return templateRepository.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get template by name.
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<Template> getTemplateByName(@PathVariable String name) {
        return templateRepository.findByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get active templates only.
     */
    @GetMapping("/active")
    public ResponseEntity<List<Template>> getActiveTemplates() {
        return ResponseEntity.ok(templateRepository.findByActive(true));
    }

    /**
     * Search templates by keyword.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Template>> searchTemplates(@RequestParam String keyword) {
        return ResponseEntity.ok(templateRepository.searchTemplates(keyword));
    }

    /**
     * Create a new template.
     */
    @PostMapping
    public ResponseEntity<Template> createTemplate(@RequestBody Template template) {
        if (templateRepository.existsByCode(template.getCode())) {
            return ResponseEntity.badRequest().build();
        }
        Template createdTemplate = templateRepository.save(template);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate);
    }

    /**
     * Update an existing template.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Template> updateTemplate(
            @PathVariable Long id,
            @RequestBody Template updatedTemplate) {
        return templateRepository.findById(id)
                .map(template -> {
                    template.setName(updatedTemplate.getName());
                    template.setCode(updatedTemplate.getCode());
                    template.setOrganizationName(updatedTemplate.getOrganizationName());
                    template.setLayout(updatedTemplate.getLayout());
                    template.setPrimaryColor(updatedTemplate.getPrimaryColor());
                    template.setSecondaryColor(updatedTemplate.getSecondaryColor());
                    template.setTextColor(updatedTemplate.getTextColor());
                    template.setTagline(updatedTemplate.getTagline());
                    template.setActive(updatedTemplate.getActive());
                    return ResponseEntity.ok(templateRepository.save(template));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a template.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        if (templateRepository.existsById(id)) {
            templateRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Toggle template active status.
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Template> toggleTemplateActive(@PathVariable Long id) {
        return templateRepository.findById(id)
                .map(template -> {
                    template.setActive(!template.getActive());
                    return ResponseEntity.ok(templateRepository.save(template));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
