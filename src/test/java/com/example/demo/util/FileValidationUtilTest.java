package com.example.demo.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class FileValidationUtilTest {

    @Test
    void testValidateValidJpegFile() {
        byte[] jpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x00};
        MultipartFile file = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", jpegContent
        );

        // Should not throw exception
        assertDoesNotThrow(() -> FileValidationUtil.validatePhotoFile(file));
    }

    @Test
    void testValidateValidPngFile() {
        byte[] pngContent = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x00, 0x00};
        MultipartFile file = new MockMultipartFile(
                "photo", "test.png", "image/png", pngContent
        );

        assertDoesNotThrow(() -> FileValidationUtil.validatePhotoFile(file));
    }

    @Test
    void testValidateEmptyFile() {
        byte[] emptyContent = new byte[0];
        MultipartFile file = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", emptyContent
        );

        assertThrows(IllegalArgumentException.class, () -> 
            FileValidationUtil.validatePhotoFile(file)
        );
    }

    @Test
    void testValidateNullFile() {
        assertThrows(IllegalArgumentException.class, () -> 
            FileValidationUtil.validatePhotoFile(null)
        );
    }

    @Test
    void testValidateFileTooLarge() {
        byte[] largeContent = new byte[5242881]; // 5MB + 1 byte
        MultipartFile file = new MockMultipartFile(
                "photo", "large.jpg", "image/jpeg", largeContent
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            FileValidationUtil.validatePhotoFile(file)
        );
        assertTrue(exception.getMessage().contains("exceeds maximum allowed size"));
    }

    @Test
    void testValidateInvalidMimeType() {
        byte[] textContent = "This is text".getBytes();
        MultipartFile file = new MockMultipartFile(
                "photo", "test.txt", "text/plain", textContent
        );

        assertThrows(IllegalArgumentException.class, () -> 
            FileValidationUtil.validatePhotoFile(file)
        );
    }

    @Test
    void testValidateInvalidExtension() {
        byte[] jpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        MultipartFile file = new MockMultipartFile(
                "photo", "test.gif", "image/jpeg", jpegContent
        );

        assertThrows(IllegalArgumentException.class, () -> 
            FileValidationUtil.validatePhotoFile(file)
        );
    }

    @Test
    void testValidateInvalidMagicNumber() {
        byte[] fakeJpegContent = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00};
        MultipartFile file = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", fakeJpegContent
        );

        assertThrows(IllegalArgumentException.class, () -> 
            FileValidationUtil.validatePhotoFile(file)
        );
    }

    @Test
    void testGetFileExtension() {
        assertEquals("jpg", FileValidationUtil.getFileExtension("test.jpg"));
        assertEquals("jpeg", FileValidationUtil.getFileExtension("photo.jpeg"));
        assertEquals("png", FileValidationUtil.getFileExtension("image.png"));
        assertEquals("", FileValidationUtil.getFileExtension("noextension"));
        assertEquals("", FileValidationUtil.getFileExtension(".hidden"));
    }

    @Test
    void testFormatFileSize() {
        assertEquals("0 B",   FileValidationUtil.formatFileSize(0));
        assertEquals("1.0 KB", FileValidationUtil.formatFileSize(1024));  // 1024 = 1.0 KB
        assertEquals("1.0 KB", FileValidationUtil.formatFileSize(1025));
        assertEquals("1.0 MB", FileValidationUtil.formatFileSize(1048576));
        assertEquals("5.0 MB", FileValidationUtil.formatFileSize(5242880));
    }

    @Test
    void testGenerateSafeFilename() {
        String safe1 = FileValidationUtil.generateSafeFilename("my photo.jpg");
        assertTrue(safe1.contains("my_photo.jpg") || safe1.contains("my photo.jpg"));
        
        String safe2 = FileValidationUtil.generateSafeFilename("photo<script>.jpg");
        assertFalse(safe2.contains("<"));
        assertFalse(safe2.contains(">"));
        
        String safe3 = FileValidationUtil.generateSafeFilename(null);
        assertEquals("unknown", safe3);
    }

    @Test
    void testGenerateUniqueFilename() {
        String unique1 = FileValidationUtil.generateUniqueFilename("photo.jpg");
        String unique2 = FileValidationUtil.generateUniqueFilename("photo.jpg");
        
        assertNotEquals(unique1, unique2);
        assertTrue(unique1.endsWith(".jpg"));
        assertTrue(unique2.endsWith(".jpg"));
        assertTrue(unique1.matches("\\d+_[a-f0-9]{8}\\.jpg"));
    }
}
