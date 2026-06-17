package com.example.demo.service;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.util.PDFUtil;
import com.example.demo.util.QRCodeUtil;
import com.itextpdf.text.DocumentException;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PhotoUploadService photoUploadService;

    @Autowired
    public ProfileService(ProfileRepository profileRepository, PhotoUploadService photoUploadService) {
        this.profileRepository = profileRepository;
        this.photoUploadService = photoUploadService;
    }

    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    public Profile getProfileById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + id));
    }

    public Profile createProfile(Profile profile, MultipartFile file) throws IOException {
        // Unique ID Generation: Format (YEAR-DEPT-UUID_SHORT)
        String currentYear = String.valueOf(Year.now().getValue());
        String dept = (profile.getDepartment() != null && !profile.getDepartment().isBlank())
                ? profile.getDepartment().replaceAll("\\s+", "").toUpperCase()
                : "GEN";
        // Limit dept prefix to 6 chars for readability
        if (dept.length() > 6) dept = dept.substring(0, 6);
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        profile.setRegistrationNumber(currentYear + "-" + dept + "-" + uniqueSuffix);

        // Generate UUID if not set
        if (profile.getUuid() == null) {
            profile.setUuid(UUID.randomUUID().toString());
        }

        // Default barcode type
        if (profile.getBarcodeType() == null) {
            profile.setBarcodeType(BarcodeType.CODE_128);
        }

        // Photo Upload Handling with validation and storage
        if (file != null && !file.isEmpty()) {
            try {
                PhotoUploadService.PhotoUploadResult uploadResult = photoUploadService.uploadPhoto(file);
                profile.setPhotoFileName(uploadResult.getStoredFilename());
                profile.setPhotoContentType(uploadResult.getContentType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Photo upload failed: " + e.getMessage());
            }
        }

        return profileRepository.save(profile);
    }

    /**
     * Full profile update (all fields). Only updates non-null values from updatedProfile.
     */
    public Profile updateProfile(Long id, Profile updatedProfile, MultipartFile file) throws IOException {
        Profile existingProfile = getProfileById(id);

        // Only overwrite fields that are provided (non-null)
        if (updatedProfile.getFullName() != null) {
            existingProfile.setFullName(updatedProfile.getFullName());
        }
        if (updatedProfile.getEmail() != null) {
            existingProfile.setEmail(updatedProfile.getEmail());
        }
        if (updatedProfile.getDepartment() != null) {
            existingProfile.setDepartment(updatedProfile.getDepartment());
        }
        if (updatedProfile.getType() != null) {
            existingProfile.setType(updatedProfile.getType());
        }
        if (updatedProfile.getTitle() != null) {
            existingProfile.setTitle(updatedProfile.getTitle());
        }
        if (updatedProfile.getPhone() != null) {
            existingProfile.setPhone(updatedProfile.getPhone());
        }
        if (updatedProfile.getBloodGroup() != null) {
            existingProfile.setBloodGroup(updatedProfile.getBloodGroup());
        }
        if (updatedProfile.getDateOfBirth() != null) {
            existingProfile.setDateOfBirth(updatedProfile.getDateOfBirth());
        }
        if (updatedProfile.getExpiryDate() != null) {
            existingProfile.setExpiryDate(updatedProfile.getExpiryDate());
        }
        if (updatedProfile.getBarcodeType() != null) {
            existingProfile.setBarcodeType(updatedProfile.getBarcodeType());
        }
        if (updatedProfile.getTemplate() != null) {
            existingProfile.setTemplate(updatedProfile.getTemplate());
        }

        // Update photo if provided
        if (file != null && !file.isEmpty()) {
            handlePhotoUpload(existingProfile, file);
        }

        return profileRepository.save(existingProfile);
    }

    /**
     * Update only the photo for an existing profile — does NOT touch any other fields.
     * Fixes the bug where uploading a photo via POST /{id}/photo wiped all profile data.
     */
    public Profile updatePhotoOnly(Long id, MultipartFile file) throws IOException {
        Profile existingProfile = getProfileById(id);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Photo file is required");
        }
        handlePhotoUpload(existingProfile, file);
        return profileRepository.save(existingProfile);
    }

    public void deleteProfile(Long id) {
        Profile profile = getProfileById(id);

        // Delete associated photo
        if (profile.getPhotoFileName() != null) {
            try {
                photoUploadService.deletePhoto(profile.getPhotoFileName());
            } catch (IOException e) {
                // Log warning but continue with profile deletion
                System.err.println("Warning: Failed to delete photo for profile " + id + ": " + e.getMessage());
            }
        }

        profileRepository.deleteById(id);
    }

    public List<Profile> searchProfiles(String keyword) {
        return profileRepository.searchProfiles(keyword);
    }

    public List<Profile> searchByType(ProfileType type) {
        return profileRepository.findByType(type);
    }

    /**
     * Generate PDF ID card for a profile.
     */
    public byte[] generatePDFCard(Long profileId) throws IOException, DocumentException {
        Profile profile = getProfileById(profileId);
        byte[] photoBytes = null;
        if (profile.hasPhoto()) {
            try {
                photoBytes = photoUploadService.getPhotoBytes(profile.getPhotoFileName());
            } catch (IOException e) {
                // Continue without photo if retrieval fails
                System.err.println("Warning: Could not load photo for PDF: " + e.getMessage());
            }
        }
        return PDFUtil.generateIDCardPDF(profile, photoBytes);
    }

    /**
     * Generate QR code for a profile (contains UUID/verification URL).
     */
    public byte[] generateQRCode(Long profileId) throws IOException, WriterException {
        Profile profile = getProfileById(profileId);
        String qrContent = "https://verify.idcard.local/" + profile.getUuid()
                + "?name=" + profile.getFullName()
                + "&reg=" + profile.getRegistrationNumber();
        return QRCodeUtil.generateQRCodeBytes(qrContent);
    }

    /**
     * Batch generate PDF cards for multiple profiles.
     */
    public byte[] generateBatchPDFCards(List<Long> profileIds) throws IOException, DocumentException {
        List<Profile> profiles = profileIds.stream()
                .map(this::getProfileById)
                .toList();
        return PDFUtil.generateBatchIDCardsPDF(profiles);
    }

    /**
     * Batch generate PDF cards for all profiles of a specific type.
     */
    public byte[] generateBatchPDFCardsByType(ProfileType type) throws IOException, DocumentException {
        List<Profile> profiles = searchByType(type);
        if (profiles.isEmpty()) {
            throw new RuntimeException("No profiles found for type: " + type);
        }
        return PDFUtil.generateBatchIDCardsPDF(profiles);
    }

    /**
     * Check if registration number already exists.
     */
    public boolean registrationNumberExists(String registrationNumber) {
        return profileRepository.existsByRegistrationNumber(registrationNumber);
    }

    /**
     * Get profile photo as byte array.
     */
    public byte[] getProfilePhoto(Long profileId) throws IOException {
        Profile profile = getProfileById(profileId);
        if (profile.getPhotoFileName() == null) {
            throw new IOException("Profile has no photo");
        }
        return photoUploadService.getPhotoBytes(profile.getPhotoFileName());
    }

    /**
     * Check if profile has a photo.
     */
    public boolean hasPhoto(Long profileId) {
        Profile profile = getProfileById(profileId);
        return profile.getPhotoFileName() != null &&
               photoUploadService.photoExists(profile.getPhotoFileName());
    }

    /**
     * Delete profile photo only.
     */
    public void deleteProfilePhoto(Long profileId) throws IOException {
        Profile profile = getProfileById(profileId);
        if (profile.getPhotoFileName() != null) {
            photoUploadService.deletePhoto(profile.getPhotoFileName());
            profile.setPhotoFileName(null);
            profile.setPhotoContentType(null);
            profileRepository.save(profile);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void handlePhotoUpload(Profile profile, MultipartFile file) throws IOException {
        try {
            // Delete old photo if it exists
            if (profile.getPhotoFileName() != null) {
                photoUploadService.deletePhoto(profile.getPhotoFileName());
            }
            // Upload new photo
            PhotoUploadService.PhotoUploadResult uploadResult = photoUploadService.uploadPhoto(file);
            profile.setPhotoFileName(uploadResult.getStoredFilename());
            profile.setPhotoContentType(uploadResult.getContentType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Photo upload failed: " + e.getMessage());
        }
    }
}