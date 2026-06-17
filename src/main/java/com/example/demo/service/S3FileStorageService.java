package com.example.demo.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * AWS S3 file storage implementation (optional).
 * 
 * To use this service:
 * 1. Add aws-java-sdk-s3 dependency to pom.xml
 * 2. Configure application.properties with AWS credentials:
 *    - app.storage.type=s3
 *    - app.aws.access-key-id=your_key
 *    - app.aws.secret-access-key=your_secret
 *    - app.aws.s3.bucket-name=your_bucket
 *    - app.aws.s3.region=us-east-1
 */
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3FileStorageService implements FileStorageService {

    // TODO: Implement S3 storage using AWS SDK
    // Add dependency: <dependency>
    //                  <groupId>software.amazon.awssdk</groupId>
    //                  <artifactId>s3</artifactId>
    //                </dependency>

    private static final String STORAGE_TYPE = "S3";

    /**
     * Initialize S3 client (to be implemented).
     */
    public S3FileStorageService() {
        // TODO: Initialize S3 client
        // this.s3Client = S3Client.builder().region(region).build();
    }

    @Override
    public String storeFile(MultipartFile file, String filename) throws IOException {
        // TODO: Implement S3 upload
        // s3Client.putObject(PutObjectRequest.builder()
        //     .bucket(bucketName)
        //     .key(filename)
        //     .build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        // return "https://" + bucketName + ".s3.amazonaws.com/" + filename;
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }

    @Override
    public byte[] retrieveFile(String filename) throws IOException {
        // TODO: Implement S3 download
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }

    @Override
    public InputStream getFileStream(String filename) throws IOException {
        // TODO: Implement S3 stream
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }

    @Override
    public void deleteFile(String filename) throws IOException {
        // TODO: Implement S3 delete
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }

    @Override
    public boolean fileExists(String filename) {
        // TODO: Implement S3 exists check
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }

    @Override
    public long getFileSize(String filename) throws IOException {
        // TODO: Implement S3 size check
        throw new UnsupportedOperationException("S3 storage not yet implemented");
    }

    @Override
    public String getStorageType() {
        return STORAGE_TYPE;
    }
}
