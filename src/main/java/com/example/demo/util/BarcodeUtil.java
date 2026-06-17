package com.example.demo.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for generating barcodes (Code-128 and EAN-13) using ZXing.
 */
public class BarcodeUtil {

    private static final int BARCODE_WIDTH  = 300;
    private static final int BARCODE_HEIGHT = 80;

    /**
     * Generate a Code-128 barcode for the given content.
     *
     * @param content The string to encode (registration number, UUID, etc.)
     * @return PNG image as byte array
     * @throws WriterException if encoding fails
     * @throws IOException     if image writing fails
     */
    public static byte[] generateCode128(String content) throws WriterException, IOException {
        return generateBarcode(content, BarcodeFormat.CODE_128, BARCODE_WIDTH, BARCODE_HEIGHT);
    }

    /**
     * Generate an EAN-13 barcode for the given content.
     * The content must be exactly 12 digits (the 13th check digit is added automatically).
     *
     * @param content 12-digit string
     * @return PNG image as byte array
     * @throws WriterException if encoding fails
     * @throws IOException     if image writing fails
     */
    public static byte[] generateEAN13(String content) throws WriterException, IOException {
        // EAN-13 requires exactly 12 or 13 digits; pad/trim if necessary
        String eanContent = sanitizeEAN(content);
        return generateBarcode(eanContent, BarcodeFormat.EAN_13, BARCODE_WIDTH, BARCODE_HEIGHT);
    }

    /**
     * Generate a barcode image as BufferedImage (for embedding in PDFs).
     *
     * @param content The content to encode
     * @param format  ZXing BarcodeFormat (CODE_128 or EAN_13)
     * @param width   Image width in pixels
     * @param height  Image height in pixels
     * @return BufferedImage of the barcode
     * @throws WriterException if encoding fails
     */
    public static BufferedImage generateBarcodeImage(String content, BarcodeFormat format,
                                                     int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1);

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix bitMatrix = writer.encode(content, format, width, height, hints);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        return image;
    }

    /**
     * Generate a Code-128 barcode as a BufferedImage.
     */
    public static BufferedImage generateCode128Image(String content) throws WriterException {
        return generateBarcodeImage(content, BarcodeFormat.CODE_128, BARCODE_WIDTH, BARCODE_HEIGHT);
    }

    /**
     * Generate an EAN-13 barcode as a BufferedImage.
     */
    public static BufferedImage generateEAN13Image(String content) throws WriterException {
        return generateBarcodeImage(sanitizeEAN(content), BarcodeFormat.EAN_13, BARCODE_WIDTH, BARCODE_HEIGHT);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static byte[] generateBarcode(String content, BarcodeFormat format,
                                          int width, int height) throws WriterException, IOException {
        BufferedImage image = generateBarcodeImage(content, format, width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    /**
     * Sanitize input to exactly 13 digits suitable for EAN-13.
     * Strips non-digits, pads with zeros, or truncates to 13 chars.
     */
    private static String sanitizeEAN(String content) {
        String digits = content.replaceAll("[^0-9]", "");
        if (digits.length() < 13) {
            digits = String.format("%013d", Long.parseLong(digits.isEmpty() ? "0" : digits));
        } else if (digits.length() > 13) {
            digits = digits.substring(0, 13);
        }
        return digits;
    }
}
