package com.example.demo.service;

import com.example.demo.config.FileUploadConfig;
import com.example.demo.util.FileValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service for handling photo uploads with validation and storage.
 */
@Service
public class PhotoUploadService {

    private final FileStorageService fileStorageService;
    private final FileUploadConfig fileUploadConfig;

    @Autowired
    public PhotoUploadService(LocalFileStorageService fileStorageService, FileUploadConfig fileUploadConfig) {
        this.fileStorageService = fileStorageService;
        this.fileUploadConfig = fileUploadConfig;
    }

    /**
     * Upload and store a photo with full validation.
     * @param file The photo file to upload
     * @return PhotoUploadResult containing filename and metadata
     * @throws IllegalArgumentException if file validation fails
     * @throws IOException if storage fails
     */
    public PhotoUploadResult uploadPhoto(MultipartFile file) throws IOException {
        // Validate file
        FileValidationUtil.validatePhotoFile(file);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = FileValidationUtil.generateUniqueFilename(originalFilename);

        // Store file
        String storagePath = fileStorageService.storeFile(file, uniqueFilename);

        // Return result with metadata
        return PhotoUploadResult.builder()
                .originalFilename(originalFilename)
                .storedFilename(uniqueFilename)
                .storagePath(storagePath)
                .fileSize(file.getSize())
                .fileSizeFormatted(FileValidationUtil.formatFileSize(file.getSize()))
                .contentType(file.getContentType())
                .storageType(fileStorageService.getStorageType())
                .uploadSuccessful(true)
                .build();
    }

    /**
     * Delete a photo.
     * @param filename The filename to delete
     * @throws IOException if deletion fails
     */
    public void deletePhoto(String filename) throws IOException {
        if (fileStorageService.fileExists(filename)) {
            fileStorageService.deleteFile(filename);
        }
    }

    /**
     * Get photo as byte array.
     * @param filename The filename to retrieve
     * @return Photo file as byte array
     * @throws IOException if retrieval fails
     */
    public byte[] getPhotoBytes(String filename) throws IOException {
        if (!fileStorageService.fileExists(filename)) {
            throw new IOException("Photo not found: " + filename);
        }
        return fileStorageService.retrieveFile(filename);
    }

    /**
     * Check if photo exists.
     * @param filename The filename to check
     * @return true if photo exists
     */
    public boolean photoExists(String filename) {
        return fileStorageService.fileExists(filename);
    }

    /**
     * Get photo file size.
     * @param filename The filename
     * @return File size in bytes
     * @throws IOException if size cannot be determined
     */
    public long getPhotoSize(String filename) throws IOException {
        return fileStorageService.getFileSize(filename);
    }

    /**
     * Get storage type being used.
     * @return Storage type identifier
     */
    public String getStorageType() {
        return fileStorageService.getStorageType();
    }

    /**
     * Result object for photo upload operations.
     */
    public static class PhotoUploadResult {
        private String originalFilename;
        private String storedFilename;
        private String storagePath;
        private long fileSize;
        private String fileSizeFormatted;
        private String contentType;
        private String storageType;
        private boolean uploadSuccessful;

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public String getOriginalFilename() {
            return originalFilename;
        }

        public String getStoredFilename() {
            return storedFilename;
        }

        public String getStoragePath() {
            return storagePath;
        }

        public long getFileSize() {
            return fileSize;
        }

        public String getFileSizeFormatted() {
            return fileSizeFormatted;
        }

        public String getContentType() {
            return contentType;
        }

        public String getStorageType() {
            return storageType;
        }

        public boolean isUploadSuccessful() {
            return uploadSuccessful;
        }

        // Builder class
        public static class Builder {
            private String originalFilename;
            private String storedFilename;
            private String storagePath;
            private long fileSize;
            private String fileSizeFormatted;
            private String contentType;
            private String storageType;
            private boolean uploadSuccessful;

            public Builder originalFilename(String originalFilename) {
                this.originalFilename = originalFilename;
                return this;
            }

            public Builder storedFilename(String storedFilename) {
                this.storedFilename = storedFilename;
                return this;
            }

            public Builder storagePath(String storagePath) {
                this.storagePath = storagePath;
                return this;
            }

            public Builder fileSize(long fileSize) {
                this.fileSize = fileSize;
                return this;
            }

            public Builder fileSizeFormatted(String fileSizeFormatted) {
                this.fileSizeFormatted = fileSizeFormatted;
                return this;
            }

            public Builder contentType(String contentType) {
                this.contentType = contentType;
                return this;
            }

            public Builder storageType(String storageType) {
                this.storageType = storageType;
                return this;
            }

            public Builder uploadSuccessful(boolean uploadSuccessful) {
                this.uploadSuccessful = uploadSuccessful;
                return this;
            }

            public PhotoUploadResult build() {
                PhotoUploadResult result = new PhotoUploadResult();
                result.originalFilename = this.originalFilename;
                result.storedFilename = this.storedFilename;
                result.storagePath = this.storagePath;
                result.fileSize = this.fileSize;
                result.fileSizeFormatted = this.fileSizeFormatted;
                result.contentType = this.contentType;
                result.storageType = this.storageType;
                result.uploadSuccessful = this.uploadSuccessful;
                return result;
            }
        }
    }
}
