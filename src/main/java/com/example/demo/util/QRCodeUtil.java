package com.example.demo.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for QR Code generation and decoding.
 */
public class QRCodeUtil {

    private static final int QR_CODE_SIZE = 300;
    private static final QRCodeWriter writer = new QRCodeWriter();

    /**
     * Generate QR code for given content.
     * @param content The content to encode
     * @return BufferedImage representation of QR code
     * @throws WriterException if encoding fails
     */
    public static BufferedImage generateQRCode(String content) throws WriterException {
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);
        return createImageFromBitMatrix(bitMatrix);
    }

    /**
     * Generate QR code and return as byte array.
     * @param content The content to encode
     * @return Byte array of the QR code image
     * @throws WriterException if encoding fails
     * @throws IOException if image writing fails
     */
    public static byte[] generateQRCodeBytes(String content) throws WriterException, IOException {
        BufferedImage qrImage = generateQRCode(content);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", baos);
        return baos.toByteArray();
    }

    /**
     * Decode QR code from byte array.
     * @param imageBytes The byte array of the QR code image
     * @return Decoded content
     * @throws IOException if image reading fails
     * @throws NotFoundException if QR code not found or invalid
     */
    public static String decodeQRCode(byte[] imageBytes) throws IOException, NotFoundException {
        BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
    }

    /**
     * Create BufferedImage from BitMatrix.
     */
    private static BufferedImage createImageFromBitMatrix(BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return image;
    }
}
