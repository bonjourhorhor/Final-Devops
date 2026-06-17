package com.example.demo.service;

import com.example.demo.config.FileUploadConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Local file storage implementation.
 */
@Service
public class LocalFileStorageService implements FileStorageService {

    private final FileUploadConfig fileUploadConfig;

    @Autowired
    public LocalFileStorageService(FileUploadConfig fileUploadConfig) {
        this.fileUploadConfig = fileUploadConfig;
        initializeStorageDirectory();
    }

    /**
     * Initialize storage directory if it doesn't exist.
     */
    private void initializeStorageDirectory() {
        Path uploadPath = Paths.get(fileUploadConfig.getUploadDir());
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize upload directory: " + e.getMessage());
        }
    }

    @Override
    public String storeFile(MultipartFile file, String filename) throws IOException {
        Path uploadPath = Paths.get(fileUploadConfig.getUploadDir());
        Path filePath = uploadPath.resolve(filename);

        try {
            // Create parent directories if needed
            Files.createDirectories(filePath.getParent());
            
            // Store the file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return relative path
            return filePath.toString();
        } catch (IOException e) {
            throw new IOException("Failed to store file: " + filename, e);
        }
    }

    @Override
    public byte[] retrieveFile(String filename) throws IOException {
        Path filePath = getValidatedFilePath(filename);
        
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filename);
        }
        
        return Files.readAllBytes(filePath);
    }

    @Override
    public InputStream getFileStream(String filename) throws IOException {
        Path filePath = getValidatedFilePath(filename);
        
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filename);
        }
        
        return Files.newInputStream(filePath);
    }

    @Override
    public void deleteFile(String filename) throws IOException {
        Path filePath = getValidatedFilePath(filename);
        
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new IOException("Failed to delete file: " + filename, e);
        }
    }

    @Override
    public boolean fileExists(String filename) {
        try {
            Path filePath = getValidatedFilePath(filename);
            return Files.exists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public long getFileSize(String filename) throws IOException {
        Path filePath = getValidatedFilePath(filename);
        
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filename);
        }
        
        return Files.size(filePath);
    }

    @Override
    public String getStorageType() {
        return "LOCAL";
    }

    /**
     * Get validated file path to prevent directory traversal attacks.
     */
    private Path getValidatedFilePath(String filename) throws IOException {
        Path uploadPath = Paths.get(fileUploadConfig.getUploadDir()).toAbsolutePath();
        Path filePath = uploadPath.resolve(filename).toAbsolutePath();
        
        // Ensure file is within upload directory
        if (!filePath.startsWith(uploadPath)) {
            throw new IOException("Invalid file path: " + filename);
        }
        
        return filePath;
    }

    /**
     * Get absolute file path.
     */
    public String getAbsoluteFilePath(String filename) throws IOException {
        return getValidatedFilePath(filename).toString();
    }

    /**
     * Get URL path for web access.
     */
    public String getFileUrl(String filename) {
        return "/" + fileUploadConfig.getUploadDir() + "/" + filename;
    }

    /**
     * Get upload directory path.
     */
    public String getUploadDirectory() {
        return fileUploadConfig.getUploadDir();
    }
}
