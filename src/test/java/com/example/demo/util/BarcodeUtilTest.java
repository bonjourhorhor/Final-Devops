package com.example.demo.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BarcodeUtil – validates Code-128 and EAN-13 barcode generation.
 */
class BarcodeUtilTest {

    @Test
    void testGenerateCode128ReturnsNonEmptyBytes() throws WriterException, IOException {
        byte[] barcode = BarcodeUtil.generateCode128("2026-ENG-ABC12");

        assertNotNull(barcode);
        assertTrue(barcode.length > 0, "Barcode byte array should not be empty");
    }

    @Test
    void testGenerateCode128IsPngFormat() throws WriterException, IOException {
        byte[] barcode = BarcodeUtil.generateCode128("TEST-CODE-128");

        // PNG files start with 0x89, 0x50, 0x4E, 0x47
        assertEquals((byte) 0x89, barcode[0]);
        assertEquals((byte) 0x50, barcode[1]);
        assertEquals((byte) 0x4E, barcode[2]);
        assertEquals((byte) 0x47, barcode[3]);
    }

    @Test
    void testGenerateEAN13ReturnsNonEmptyBytes() throws WriterException, IOException {
        byte[] barcode = BarcodeUtil.generateEAN13("123456789012");

        assertNotNull(barcode);
        assertTrue(barcode.length > 0, "EAN-13 barcode byte array should not be empty");
    }

    @Test
    void testGenerateEAN13IsPngFormat() throws WriterException, IOException {
        byte[] barcode = BarcodeUtil.generateEAN13("123456789012");

        // PNG magic bytes
        assertEquals((byte) 0x89, barcode[0]);
        assertEquals((byte) 0x50, barcode[1]);
    }

    @Test
    void testCode128WithSpecialChars_AlphaNumericDash() throws WriterException, IOException {
        // Code-128 supports alphanumeric + special chars
        byte[] barcode = BarcodeUtil.generateCode128("2026-DEPT-XY99");

        assertNotNull(barcode);
        assertTrue(barcode.length > 0);
    }

    @Test
    void testCode128ImageDimensions() throws WriterException {
        BufferedImage img = BarcodeUtil.generateCode128Image("TEST");

        assertNotNull(img);
        assertEquals(300, img.getWidth(),  "Expected width 300px");
        assertEquals(80,  img.getHeight(), "Expected height 80px");
    }

    @Test
    void testEAN13ImageDimensions() throws WriterException {
        BufferedImage img = BarcodeUtil.generateEAN13Image("123456789012");

        assertNotNull(img);
        assertEquals(300, img.getWidth());
        assertEquals(80,  img.getHeight());
    }

    @Test
    void testEAN13WithShortInput() throws WriterException, IOException {
        // Should pad to 13 digits without throwing
        byte[] barcode = BarcodeUtil.generateEAN13("123");

        assertNotNull(barcode);
        assertTrue(barcode.length > 0);
    }

    @Test
    void testEAN13WithTooLongInput() {
        // Truncating to 13 digits may produce an invalid EAN-13 checksum;
        // the utility should either succeed or throw a descriptive exception.
        // We verify the call does not crash with an unexpected exception type.
        try {
            byte[] barcode = BarcodeUtil.generateEAN13("12345678901234567");
            assertNotNull(barcode);
        } catch (IllegalArgumentException | WriterException e) {
            // Acceptable: invalid checksum after truncation
            assertTrue(e.getMessage() != null);
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test
    void testBarcodeImageNotNull_Code128() throws WriterException {
        BufferedImage img = BarcodeUtil.generateBarcodeImage(
                "REG-NUMBER-001", BarcodeFormat.CODE_128, 300, 80);

        assertNotNull(img);
        assertEquals(BufferedImage.TYPE_INT_RGB, img.getType());
    }
}
