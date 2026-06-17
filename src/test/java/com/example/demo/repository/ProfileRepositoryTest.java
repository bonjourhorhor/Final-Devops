package com.example.demo.repository;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest

class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    private Profile testProfile;

    @BeforeEach
    void setUp() {
        profileRepository.deleteAll();

        testProfile = Profile.builder()
                .uuid("repo-test-uuid-001")
                .fullName("John Doe")
                .email("john@example.com")
                .department("Engineering")
                .type(ProfileType.EMPLOYEE)
                .registrationNumber("2026-ENG-12345")
                .issueDate(LocalDate.now())
                .build();
    }

    @Test
    void testSaveProfile() {
        Profile saved = profileRepository.save(testProfile);

        assertNotNull(saved.getId());
        assertEquals("John Doe", saved.getFullName());
    }

    @Test
    void testFindByRegistrationNumber() {
        profileRepository.save(testProfile);

        Optional<Profile> found = profileRepository.findByRegistrationNumber("2026-ENG-12345");

        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getFullName());
    }

    @Test
    void testFindByRegistrationNumberNotFound() {
        Optional<Profile> found = profileRepository.findByRegistrationNumber("NONEXISTENT");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByType() {
        profileRepository.save(testProfile);

        Profile student = Profile.builder()
                .uuid("repo-test-uuid-002")
                .fullName("Jane Smith")
                .email("jane@example.com")
                .department("Computer Science")
                .type(ProfileType.STUDENT)
                .registrationNumber("2026-CS-54321")
                .issueDate(LocalDate.now())
                .build();
        profileRepository.save(student);

        List<Profile> employees = profileRepository.findByType(ProfileType.EMPLOYEE);
        List<Profile> students = profileRepository.findByType(ProfileType.STUDENT);

        assertEquals(1, employees.size());
        assertEquals(1, students.size());
        assertEquals("John Doe", employees.get(0).getFullName());
        assertEquals("Jane Smith", students.get(0).getFullName());
    }

    @Test
    void testSearchProfiles() {
        profileRepository.save(testProfile);

        Profile another = Profile.builder()
                .uuid("repo-test-uuid-003")
                .fullName("Jane Doe")
                .email("jane@example.com")
                .department("HR")
                .type(ProfileType.EMPLOYEE)
                .registrationNumber("2026-HR-67890")
                .issueDate(LocalDate.now())
                .build();
        profileRepository.save(another);

        List<Profile> results = profileRepository.searchProfiles("Doe");

        assertEquals(2, results.size());
    }

    @Test
    void testExistsByRegistrationNumber() {
        profileRepository.save(testProfile);

        assertTrue(profileRepository.existsByRegistrationNumber("2026-ENG-12345"));
        assertFalse(profileRepository.existsByRegistrationNumber("NONEXISTENT"));
    }

    @Test
    void testFindByUuid() {
        String uuid = "repo-test-uuid-001";  // matches setUp uuid
        profileRepository.save(testProfile);

        Optional<Profile> found = profileRepository.findByUuid(uuid);

        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getFullName());
    }
}
