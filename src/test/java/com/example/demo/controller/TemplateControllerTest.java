package com.example.demo.controller;

import com.example.demo.model.Template;
import com.example.demo.repository.TemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Template testTemplate;

    @BeforeEach
    void setUp() {
        templateRepository.deleteAll();

        testTemplate = Template.builder()
                .code("TEST_BLUE")
                .name("Test Blue")
                .organizationName("Test Org")
                .layout("VERTICAL")
                .primaryColor("#1d4ed8")
                .secondaryColor("#dbeafe")
                .textColor("#1e3a5f")
                .tagline("Test Tagline")
                .active(true)
                .build();
    }

    @Test
    void testGetAllTemplates() throws Exception {
        templateRepository.save(testTemplate);

        mockMvc.perform(get("/api/templates")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetAllTemplatesEmpty() throws Exception {
        mockMvc.perform(get("/api/templates")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetTemplateById() throws Exception {
        Template saved = templateRepository.save(testTemplate);

        mockMvc.perform(get("/api/templates/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TEST_BLUE"))
                .andExpect(jsonPath("$.name").value("Test Blue"))
                .andExpect(jsonPath("$.organizationName").value("Test Org"));
    }

    @Test
    void testGetTemplateByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/templates/9999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTemplateByCode() throws Exception {
        templateRepository.save(testTemplate);

        mockMvc.perform(get("/api/templates/code/TEST_BLUE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TEST_BLUE"));
    }

    @Test
    void testGetTemplateByCodeNotFound() throws Exception {
        mockMvc.perform(get("/api/templates/code/NONEXISTENT")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetActiveTemplates() throws Exception {
        Template active = templateRepository.save(testTemplate);

        Template inactive = Template.builder()
                .code("INACTIVE_TPL")
                .name("Inactive Template")
                .layout("VERTICAL")
                .primaryColor("#000000")
                .secondaryColor("#ffffff")
                .textColor("#333333")
                .active(false)
                .build();
        templateRepository.save(inactive);

        mockMvc.perform(get("/api/templates/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("TEST_BLUE"));
    }

    @Test
    void testCreateTemplate() throws Exception {
        Template newTemplate = Template.builder()
                .code("NEW_DARK")
                .name("New Dark")
                .organizationName("New Org")
                .layout("VERTICAL")
                .primaryColor("#111827")
                .secondaryColor("#374151")
                .textColor("#f9fafb")
                .active(true)
                .build();

        mockMvc.perform(post("/api/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTemplate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("NEW_DARK"))
                .andExpect(jsonPath("$.name").value("New Dark"));
    }

    @Test
    void testCreateTemplateDuplicateCode() throws Exception {
        templateRepository.save(testTemplate);

        Template duplicate = Template.builder()
                .code("TEST_BLUE")  // same code
                .name("Another Template")
                .layout("VERTICAL")
                .primaryColor("#000000")
                .secondaryColor("#ffffff")
                .textColor("#333333")
                .active(true)
                .build();

        mockMvc.perform(post("/api/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateTemplate() throws Exception {
        Template saved = templateRepository.save(testTemplate);

        Template update = Template.builder()
                .code("TEST_BLUE_UPDATED")
                .name("Updated Blue")
                .organizationName("Updated Org")
                .layout("VERTICAL")
                .primaryColor("#2563eb")
                .secondaryColor("#bfdbfe")
                .textColor("#1e40af")
                .active(true)
                .build();

        mockMvc.perform(put("/api/templates/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Blue"))
                .andExpect(jsonPath("$.code").value("TEST_BLUE_UPDATED"));
    }

    @Test
    void testDeleteTemplate() throws Exception {
        Template saved = templateRepository.save(testTemplate);

        mockMvc.perform(delete("/api/templates/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        assert !templateRepository.existsById(saved.getId());
    }

    @Test
    void testDeleteTemplateNotFound() throws Exception {
        mockMvc.perform(delete("/api/templates/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testToggleTemplateActive() throws Exception {
        Template saved = templateRepository.save(testTemplate);

        // Toggle from true → false
        mockMvc.perform(patch("/api/templates/{id}/toggle", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Toggle from false → true
        mockMvc.perform(patch("/api/templates/{id}/toggle", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void testSearchTemplates() throws Exception {
        templateRepository.save(testTemplate);

        mockMvc.perform(get("/api/templates/search")
                .param("keyword", "Blue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Blue"));
    }

    @Test
    void testSearchTemplatesNoMatch() throws Exception {
        templateRepository.save(testTemplate);

        mockMvc.perform(get("/api/templates/search")
                .param("keyword", "NOMATCH_XYZ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
