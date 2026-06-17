package com.example.demo.controller;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.service.ProfileService;
import com.google.zxing.WriterException;
import com.itextpdf.text.DocumentException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Get all profiles.
     */
    @GetMapping
    public ResponseEntity<List<Profile>> getAllProfiles() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }

    /**
     * Get profile by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Profile> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getProfileById(id));
    }

    /**
     * Create a new profile with optional photo upload.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Profile> createProfile(
            @RequestParam(required = false) MultipartFile photo,
            @RequestParam String fullName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam ProfileType type,
            @RequestParam(required = false) Long templateId) throws IOException {

        Profile profile = Profile.builder()
                .fullName(fullName)
                .department(department)
                .title(title)
                .email(email)
                .phone(phone)
                .bloodGroup(bloodGroup)
                .type(type)
                .build();

        Profile createdProfile = profileService.createProfile(profile, photo);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProfile);
    }

    /**
     * Update an existing profile (JSON body) with optional photo update.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Profile> updateProfile(
            @PathVariable Long id,
            @RequestBody Profile updatedProfile) throws IOException {

        Profile updated = profileService.updateProfile(id, updatedProfile, null);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a profile.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search profiles by keyword.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Profile>> searchProfiles(@RequestParam String keyword) {
        return ResponseEntity.ok(profileService.searchProfiles(keyword));
    }

    /**
     * Get profiles by type (STUDENT, EMPLOYEE, USER).
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Profile>> getProfilesByType(@PathVariable ProfileType type) {
        return ResponseEntity.ok(profileService.searchByType(type));
    }

    /**
     * Generate PDF ID card for a profile.
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePDF(@PathVariable Long id) throws IOException, DocumentException {
        byte[] pdfBytes = profileService.generatePDFCard(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("id-card-" + id + ".pdf")
                        .build()
        );
        headers.set("Content-Type", "application/pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    /**
     * Generate QR code for a profile.
     */
    @GetMapping("/{id}/qrcode")
    public ResponseEntity<byte[]> generateQRCode(@PathVariable Long id) throws IOException, WriterException {
        byte[] qrBytes = profileService.generateQRCode(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("qr-code-" + id + ".png")
                        .build()
        );
        headers.set("Content-Type", "image/png");

        return ResponseEntity.ok()
                .headers(headers)
                .body(qrBytes);
    }

    /**
     * Generate batch PDF cards for specific profiles.
     */
    @PostMapping("/batch/pdf")
    public ResponseEntity<byte[]> generateBatchPDF(@RequestBody List<Long> profileIds)
            throws IOException, DocumentException {
        byte[] pdfBytes = profileService.generateBatchPDFCards(profileIds);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("id-cards-batch.pdf")
                        .build()
        );
        headers.set("Content-Type", "application/pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    /**
     * Generate batch PDF cards for all profiles of a specific type.
     */
    @GetMapping("/batch/pdf/type/{type}")
    public ResponseEntity<byte[]> generateBatchPDFByType(@PathVariable ProfileType type)
            throws IOException, DocumentException {
        byte[] pdfBytes = profileService.generateBatchPDFCardsByType(type);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("id-cards-" + type + ".pdf")
                        .build()
        );
        headers.set("Content-Type", "application/pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    /**
     * Check if registration number exists.
     */
    @GetMapping("/check/registration/{regNumber}")
    public ResponseEntity<Boolean> checkRegistrationNumberExists(@PathVariable String regNumber) {
        return ResponseEntity.ok(profileService.registrationNumberExists(regNumber));
    }

    /**
     * Download profile photo.
     */
    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getProfilePhoto(@PathVariable Long id) throws IOException {
        byte[] photoBytes = profileService.getProfilePhoto(id);
        Profile profile = profileService.getProfileById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("profile-" + id + ".jpg")
                        .build()
        );
        headers.set("Content-Type", profile.getPhotoContentType() != null ?
                profile.getPhotoContentType() : "image/jpeg");

        return ResponseEntity.ok()
                .headers(headers)
                .body(photoBytes);
    }

    /**
     * Check if profile has photo.
     */
    @GetMapping("/{id}/has-photo")
    public ResponseEntity<Boolean> hasPhoto(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.hasPhoto(id));
    }

    /**
     * Delete profile photo only.
     */
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<Void> deleteProfilePhoto(@PathVariable Long id) throws IOException {
        profileService.deleteProfilePhoto(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload/replace photo for existing profile — FIXED: does not overwrite other profile fields.
     */
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestParam MultipartFile photo) throws IOException {

        if (photo == null || photo.isEmpty()) {
            return ResponseEntity.badRequest().body("Photo file is required");
        }

        try {
            Profile updated = profileService.updatePhotoOnly(id, photo);
            return ResponseEntity.ok()
                    .body("Photo uploaded successfully. File: " + updated.getPhotoFileName());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Photo upload failed: " + e.getMessage());
        }
    }
}
