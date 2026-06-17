package com.example.demo.controller;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
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
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Profile testProfile;

    @BeforeEach
    void setUp() {
        profileRepository.deleteAll();

        testProfile = Profile.builder()
                .uuid("ctrl-test-uuid-001")
                .fullName("John Doe")
                .email("john@example.com")
                .department("Engineering")
                .type(ProfileType.EMPLOYEE)
                .registrationNumber("2026-ENG-CTRL1")
                .issueDate(LocalDate.now())
                .build();
    }

    @Test
    void testGetAllProfiles() throws Exception {
        profileRepository.save(testProfile);

        mockMvc.perform(get("/api/profiles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetProfileById() throws Exception {
        Profile saved = profileRepository.save(testProfile);

        mockMvc.perform(get("/api/profiles/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testGetProfileByIdNotFound() throws Exception {
        // GlobalExceptionHandler maps "not found" RuntimeException → 404
        mockMvc.perform(get("/api/profiles/99999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchProfiles() throws Exception {
        profileRepository.save(testProfile);

        mockMvc.perform(get("/api/profiles/search")
                .param("keyword", "John")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));
    }

    @Test
    void testSearchProfilesNoMatch() throws Exception {
        profileRepository.save(testProfile);

        mockMvc.perform(get("/api/profiles/search")
                .param("keyword", "NOMATCH_XYZ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetProfilesByType() throws Exception {
        profileRepository.save(testProfile);

        mockMvc.perform(get("/api/profiles/type/{type}", "EMPLOYEE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetProfilesByTypeStudentEmpty() throws Exception {
        profileRepository.save(testProfile); // EMPLOYEE only

        mockMvc.perform(get("/api/profiles/type/{type}", "STUDENT")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testDeleteProfile() throws Exception {
        Profile saved = profileRepository.save(testProfile);

        mockMvc.perform(delete("/api/profiles/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assert !profileRepository.existsById(saved.getId());
    }

    @Test
    void testCheckRegistrationNumberExists() throws Exception {
        profileRepository.save(testProfile);

        mockMvc.perform(get("/api/profiles/check/registration/{regNumber}",
                        testProfile.getRegistrationNumber())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void testCheckRegistrationNumberDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/profiles/check/registration/NONEXISTENT-999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    void testGeneratePDF() throws Exception {
        Profile saved = profileRepository.save(testProfile);

        mockMvc.perform(get("/api/profiles/{id}/pdf", saved.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    void testGenerateQRCode() throws Exception {
        Profile saved = profileRepository.save(testProfile);

        mockMvc.perform(get("/api/profiles/{id}/qrcode", saved.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));
    }

    @Test
    void testHasPhoto_NoPhoto() throws Exception {
        Profile saved = profileRepository.save(testProfile);

        mockMvc.perform(get("/api/profiles/{id}/has-photo", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
}
