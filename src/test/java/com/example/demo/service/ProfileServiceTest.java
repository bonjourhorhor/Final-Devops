package com.example.demo.service;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.repository.ProfileRepository;
import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PhotoUploadService photoUploadService;

    // Manually construct the service so both dependencies are injected
    private ProfileService profileService;

    private Profile testProfile;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(profileRepository, photoUploadService);

        testProfile = Profile.builder()
                .id(1L)
                .uuid("test-uuid-1234")
                .fullName("John Doe")
                .email("john@example.com")
                .department("Engineering")
                .type(ProfileType.EMPLOYEE)
                .registrationNumber("2026-ENG-ABC01")
                .build();
    }

    @Test
    void testGetAllProfiles() {
        List<Profile> profiles = Arrays.asList(testProfile);
        when(profileRepository.findAll()).thenReturn(profiles);

        List<Profile> result = profileService.getAllProfiles();

        assertEquals(1, result.size());
        assertEquals(testProfile.getFullName(), result.get(0).getFullName());
        verify(profileRepository, times(1)).findAll();
    }

    @Test
    void testGetProfileById() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));

        Profile result = profileService.getProfileById(1L);

        assertNotNull(result);
        assertEquals(testProfile.getId(), result.getId());
        verify(profileRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProfileByIdNotFound() {
        when(profileRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> profileService.getProfileById(999L));
        assertTrue(ex.getMessage().contains("not found"));
        verify(profileRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateProfile() throws IOException {
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        Profile input = Profile.builder()
                .fullName("John Doe")
                .department("Engineering")
                .type(ProfileType.EMPLOYEE)
                .build();

        Profile result = profileService.createProfile(input, null);

        assertNotNull(result);
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    void testCreateProfileSetsRegistrationNumber() throws IOException {
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        Profile input = Profile.builder()
                .fullName("Alice Smith")
                .department("Finance")
                .type(ProfileType.EMPLOYEE)
                .build();

        Profile result = profileService.createProfile(input, null);

        assertNotNull(result.getRegistrationNumber());
        // Format: YEAR-DEPTSHORT-XXXXX e.g. 2026-FINANC-AB123
        assertTrue(result.getRegistrationNumber().startsWith("2026-"),
                "Registration number should start with current year");
        assertTrue(result.getRegistrationNumber().contains("FINANC"),
                "Registration number should contain dept prefix");
    }

    @Test
    void testCreateProfileSetsUUID() throws IOException {
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        Profile input = Profile.builder()
                .fullName("Bob")
                .type(ProfileType.STUDENT)
                .build();

        Profile result = profileService.createProfile(input, null);

        assertNotNull(result.getUuid());
        assertFalse(result.getUuid().isBlank());
    }

    @Test
    void testCreateProfileWithValidPhoto() throws IOException {
        // Valid JPEG bytes
        byte[] jpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x00};
        MultipartFile photo = new MockMultipartFile("photo", "test.jpg", "image/jpeg", jpegContent);

        PhotoUploadService.PhotoUploadResult uploadResult = PhotoUploadService.PhotoUploadResult.builder()
                .storedFilename("unique-test.jpg")
                .contentType("image/jpeg")
                .uploadSuccessful(true)
                .build();
        when(photoUploadService.uploadPhoto(any())).thenReturn(uploadResult);
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        Profile input = Profile.builder().fullName("Carol").type(ProfileType.EMPLOYEE).build();
        Profile result = profileService.createProfile(input, photo);

        assertNotNull(result);
        assertEquals("unique-test.jpg", result.getPhotoFileName());
        verify(photoUploadService, times(1)).uploadPhoto(any());
    }

    @Test
    void testCreateProfileInvalidPhotoType() throws IOException {
        byte[] fileContent = "not an image".getBytes();
        MultipartFile photo = new MockMultipartFile("photo", "test.txt", "text/plain", fileContent);

        when(photoUploadService.uploadPhoto(any()))
                .thenThrow(new IllegalArgumentException("Invalid file type"));

        Profile input = Profile.builder().fullName("Dave").type(ProfileType.STUDENT).build();

        assertThrows(IllegalArgumentException.class, () ->
                profileService.createProfile(input, photo)
        );
    }

    @Test
    void testUpdateProfileNullSafeFields() throws IOException {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        // Update only fullName — other fields should NOT be nullified
        Profile updateData = Profile.builder()
                .fullName("Jane Doe")
                .build();

        Profile result = profileService.updateProfile(1L, updateData, null);

        assertNotNull(result);
        assertEquals("Jane Doe", result.getFullName());
        // Original email should be preserved (not overwritten with null)
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void testUpdateProfileAllFields() throws IOException {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        Profile updateData = Profile.builder()
                .fullName("Jane Doe")
                .email("jane@example.com")
                .department("HR")
                .type(ProfileType.STUDENT)
                .build();

        Profile result = profileService.updateProfile(1L, updateData, null);

        assertEquals("Jane Doe", result.getFullName());
        assertEquals("jane@example.com", result.getEmail());
        assertEquals("HR", result.getDepartment());
    }

    @Test
    void testUpdatePhotoOnly() throws IOException {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        byte[] jpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        MultipartFile photo = new MockMultipartFile("photo", "new.jpg", "image/jpeg", jpegContent);

        PhotoUploadService.PhotoUploadResult uploadResult = PhotoUploadService.PhotoUploadResult.builder()
                .storedFilename("new.jpg")
                .contentType("image/jpeg")
                .uploadSuccessful(true)
                .build();
        when(photoUploadService.uploadPhoto(any())).thenReturn(uploadResult);

        Profile result = profileService.updatePhotoOnly(1L, photo);

        // Only photo changed; name & email remain intact
        assertEquals("John Doe",          result.getFullName());
        assertEquals("john@example.com",  result.getEmail());
        assertEquals("new.jpg",           result.getPhotoFileName());
    }

    @Test
    void testDeleteProfile() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        doNothing().when(profileRepository).deleteById(1L);

        profileService.deleteProfile(1L);

        verify(profileRepository, times(1)).findById(1L);
        verify(profileRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteProfileWithPhoto() throws IOException {
        testProfile.setPhotoFileName("photo.jpg");
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        doNothing().when(profileRepository).deleteById(1L);
        doNothing().when(photoUploadService).deletePhoto(anyString());

        profileService.deleteProfile(1L);

        verify(photoUploadService, times(1)).deletePhoto("photo.jpg");
        verify(profileRepository, times(1)).deleteById(1L);
    }

    @Test
    void testSearchProfiles() {
        List<Profile> profiles = Arrays.asList(testProfile);
        when(profileRepository.searchProfiles("John")).thenReturn(profiles);

        List<Profile> result = profileService.searchProfiles("John");

        assertEquals(1, result.size());
        verify(profileRepository, times(1)).searchProfiles("John");
    }

    @Test
    void testSearchByType() {
        List<Profile> profiles = Arrays.asList(testProfile);
        when(profileRepository.findByType(ProfileType.EMPLOYEE)).thenReturn(profiles);

        List<Profile> result = profileService.searchByType(ProfileType.EMPLOYEE);

        assertEquals(1, result.size());
        verify(profileRepository, times(1)).findByType(ProfileType.EMPLOYEE);
    }

    @Test
    void testGeneratePDFCard() throws IOException, DocumentException {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));

        byte[] result = profileService.generatePDFCard(1L);

        assertNotNull(result);
        assertTrue(result.length > 0, "PDF bytes should not be empty");
        verify(profileRepository, times(1)).findById(1L);
    }

    @Test
    void testGenerateQRCode() throws Exception {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));

        byte[] result = profileService.generateQRCode(1L);

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(profileRepository, times(1)).findById(1L);
    }

    @Test
    void testGenerateBatchPDFCards() throws IOException, DocumentException {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));

        byte[] result = profileService.generateBatchPDFCards(Arrays.asList(1L));

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testGenerateBatchPDFCardsByType() throws IOException, DocumentException {
        List<Profile> profiles = Arrays.asList(testProfile);
        when(profileRepository.findByType(ProfileType.EMPLOYEE)).thenReturn(profiles);

        byte[] result = profileService.generateBatchPDFCardsByType(ProfileType.EMPLOYEE);

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(profileRepository, times(1)).findByType(ProfileType.EMPLOYEE);
    }

    @Test
    void testGenerateBatchPDFCardsByTypeEmpty() {
        when(profileRepository.findByType(ProfileType.STUDENT)).thenReturn(Arrays.asList());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> profileService.generateBatchPDFCardsByType(ProfileType.STUDENT));
        assertTrue(ex.getMessage().contains("No profiles found"));
    }

    @Test
    void testRegistrationNumberExists() {
        when(profileRepository.existsByRegistrationNumber("2026-ENG-ABC01")).thenReturn(true);

        boolean result = profileService.registrationNumberExists("2026-ENG-ABC01");

        assertTrue(result);
        verify(profileRepository, times(1)).existsByRegistrationNumber("2026-ENG-ABC01");
    }

    @Test
    void testHasPhoto_WhenPhotoExists() {
        testProfile.setPhotoFileName("test.jpg");
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));
        when(photoUploadService.photoExists("test.jpg")).thenReturn(true);

        assertTrue(profileService.hasPhoto(1L));
    }

    @Test
    void testHasPhoto_WhenNoPhoto() {
        testProfile.setPhotoFileName(null);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(testProfile));

        assertFalse(profileService.hasPhoto(1L));
    }
}
