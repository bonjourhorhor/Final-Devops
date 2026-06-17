package com.example.demo.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for file upload validation.
 */
public class FileValidationUtil {

    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg",
            "image/png"
    ));

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg",
            "jpeg",
            "png"
    ));

    private static final long MAX_FILE_SIZE = 5242880; // 5MB

    /**
     * Validate file for photo upload.
     * @param file The file to validate
     * @throws IllegalArgumentException if file is invalid
     */
    public static void validatePhotoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB. Actual size: " + 
                    formatFileSize(file.getSize()));
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG and PNG are allowed. Got: " + contentType);
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !hasValidExtension(filename)) {
            throw new IllegalArgumentException("Invalid file extension. Only .jpg, .jpeg, and .png are allowed");
        }

        // Validate file magic number (first few bytes)
        validateFileMagicNumber(file, contentType);
    }

    /**
     * Check if file has valid extension.
     */
    private static boolean hasValidExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * Get file extension from filename.
     */
    public static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * Validate file magic number (file signature).
     */
    private static void validateFileMagicNumber(MultipartFile file, String contentType) {
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length < 4) {
                throw new IllegalArgumentException("File is too small to be a valid image");
            }

            // Check JPEG signature: FFD8FF
            if (contentType.contains("jpeg")) {
                if (!(bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF)) {
                    throw new IllegalArgumentException("File signature does not match JPEG format");
                }
            }
            // Check PNG signature: 89504E47
            else if (contentType.contains("png")) {
                if (!(bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47)) {
                    throw new IllegalArgumentException("File signature does not match PNG format");
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error validating file: " + e.getMessage());
        }
    }

    /**
     * Format file size for display.
     */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * Generate safe filename from original filename.
     */
    public static String generateSafeFilename(String originalFilename) {
        if (originalFilename == null) {
            return "unknown";
        }
        // Remove path separators and special characters
        String filename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        // Limit length
        if (filename.length() > 255) {
            filename = filename.substring(0, 255);
        }
        return filename;
    }

    /**
     * Generate unique filename with timestamp and UUID.
     */
    public static String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = System.currentTimeMillis() + "";
        String uuid = java.util.UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + "." + extension;
    }
}
