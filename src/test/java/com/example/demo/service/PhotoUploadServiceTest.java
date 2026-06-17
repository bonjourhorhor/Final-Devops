package com.example.demo.service;

import com.example.demo.config.FileUploadConfig;
import com.example.demo.util.FileValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PhotoUploadServiceTest {

    @Mock
    private FileUploadConfig fileUploadConfig;

    private PhotoUploadService photoUploadService;
    private LocalFileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileUploadConfig = new FileUploadConfig();
        fileUploadConfig.setUploadDir("test-uploads");
        fileUploadConfig.setMaxFileSize(5242880); // 5MB
        
        fileStorageService = new LocalFileStorageService(fileUploadConfig);
        photoUploadService = new PhotoUploadService(fileStorageService, fileUploadConfig);
    }

    @Test
    void testUploadValidJpegPhoto() throws IOException {
        byte[] jpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x00};
        MultipartFile photo = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", jpegContent
        );

        PhotoUploadService.PhotoUploadResult result = photoUploadService.uploadPhoto(photo);

        assertNotNull(result);
        assertTrue(result.isUploadSuccessful());
        assertEquals("image/jpeg", result.getContentType());
        assertNotNull(result.getStoredFilename());
        assertTrue(result.getStoredFilename().endsWith(".jpg"));
    }

    @Test
    void testUploadValidPngPhoto() throws IOException {
        byte[] pngContent = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x00, 0x00};
        MultipartFile photo = new MockMultipartFile(
                "photo", "test.png", "image/png", pngContent
        );

        PhotoUploadService.PhotoUploadResult result = photoUploadService.uploadPhoto(photo);

        assertNotNull(result);
        assertTrue(result.isUploadSuccessful());
        assertEquals("image/png", result.getContentType());
    }

    @Test
    void testUploadEmptyFile() {
        byte[] emptyContent = new byte[0];
        MultipartFile photo = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", emptyContent
        );

        assertThrows(IllegalArgumentException.class, () -> 
            photoUploadService.uploadPhoto(photo)
        );
    }

    @Test
    void testUploadFileTooLarge() {
        // Create a mock file larger than 5MB
        byte[] largeContent = new byte[5242881]; // 5MB + 1 byte
        MultipartFile photo = new MockMultipartFile(
                "photo", "large.jpg", "image/jpeg", largeContent
        );

        assertThrows(IllegalArgumentException.class, () -> 
            photoUploadService.uploadPhoto(photo)
        );
    }

    @Test
    void testUploadInvalidMimeType() {
        byte[] fileContent = "not a real image".getBytes();
        MultipartFile photo = new MockMultipartFile(
                "photo", "test.txt", "text/plain", fileContent
        );

        assertThrows(IllegalArgumentException.class, () -> 
            photoUploadService.uploadPhoto(photo)
        );
    }

    @Test
    void testUploadInvalidExtension() {
        byte[] jpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        MultipartFile photo = new MockMultipartFile(
                "photo", "test.gif", "image/jpeg", jpegContent
        );

        assertThrows(IllegalArgumentException.class, () -> 
            photoUploadService.uploadPhoto(photo)
        );
    }

    @Test
    void testPhotoExists() throws IOException {
        byte[] jpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        MultipartFile photo = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", jpegContent
        );

        PhotoUploadService.PhotoUploadResult result = photoUploadService.uploadPhoto(photo);
        boolean exists = photoUploadService.photoExists(result.getStoredFilename());

        assertTrue(exists);
    }

    @Test
    void testDeletePhoto() throws IOException {
        byte[] jpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        MultipartFile photo = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", jpegContent
        );

        PhotoUploadService.PhotoUploadResult result = photoUploadService.uploadPhoto(photo);
        photoUploadService.deletePhoto(result.getStoredFilename());
        
        assertFalse(photoUploadService.photoExists(result.getStoredFilename()));
    }

    @Test
    void testGetPhotoBytes() throws IOException {
        byte[] jpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x11, 0x22};
        MultipartFile photo = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", jpegContent
        );

        PhotoUploadService.PhotoUploadResult result = photoUploadService.uploadPhoto(photo);
        byte[] retrievedContent = photoUploadService.getPhotoBytes(result.getStoredFilename());

        assertArrayEquals(jpegContent, retrievedContent);
    }
}
