package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for file storage operations (supports local and cloud storage).
 */
public interface FileStorageService {

    /**
     * Store a file.
     * @param file The file to store
     * @param filename The filename to use
     * @return The stored file path/URL
     * @throws IOException if storage fails
     */
    String storeFile(MultipartFile file, String filename) throws IOException;

    /**
     * Retrieve a file.
     * @param filename The filename to retrieve
     * @return File content as byte array
     * @throws IOException if retrieval fails
     */
    byte[] retrieveFile(String filename) throws IOException;

    /**
     * Get file as InputStream.
     * @param filename The filename
     * @return InputStream of file
     * @throws IOException if retrieval fails
     */
    InputStream getFileStream(String filename) throws IOException;

    /**
     * Delete a file.
     * @param filename The filename to delete
     * @throws IOException if deletion fails
     */
    void deleteFile(String filename) throws IOException;

    /**
     * Check if file exists.
     * @param filename The filename to check
     * @return true if file exists
     */
    boolean fileExists(String filename);

    /**
     * Get file size in bytes.
     * @param filename The filename
     * @return File size in bytes
     * @throws IOException if size cannot be determined
     */
    long getFileSize(String filename) throws IOException;

    /**
     * Get storage type (local, s3, etc).
     * @return Storage type identifier
     */
    String getStorageType();
}
