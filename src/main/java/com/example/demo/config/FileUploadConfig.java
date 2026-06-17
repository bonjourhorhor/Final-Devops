package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for file upload settings.
 */
@Configuration
@ConfigurationProperties(prefix = "app.file")
public class FileUploadConfig {

    private String uploadDir = "uploads/photos";
    private long maxFileSize = 5242880; // 5MB in bytes
    private String[] allowedMimeTypes = {"image/jpeg", "image/png"};
    private String[] allowedExtensions = {"jpg", "jpeg", "png"};

    // Getters and Setters
    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String[] getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(String[] allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }

    public String[] getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(String[] allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }
}
