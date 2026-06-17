package com.example.demo.util;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.Template;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for generating professional ID cards in PDF format.
 * Each card embeds: photo, QR code, barcode, profile info, and template colours.
 */
public class PDFUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // A6 card dimensions (105 × 148 mm) in points (1 pt = 0.352 mm)
    private static final float CARD_W = 297.6f; // 105 mm
    private static final float CARD_H = 419.5f; // 148 mm

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Generate a PDF ID card for a single profile.
     *
     * @param profile    The profile to render
     * @param photoBytes Raw bytes of the profile photo (may be null)
     * @return PDF as byte array
     */
    public static byte[] generateIDCardPDF(Profile profile, byte[] photoBytes)
            throws IOException, DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document doc = new Document(new Rectangle(CARD_W, CARD_H), 12, 12, 12, 12);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        doc.open();

        renderCard(writer.getDirectContent(), doc, profile, photoBytes);

        doc.close();
        return baos.toByteArray();
    }

    /**
     * Overload for backward-compatibility (no photo bytes).
     */
    public static byte[] generateIDCardPDF(Profile profile)
            throws IOException, DocumentException {
        return generateIDCardPDF(profile, null);
    }

    /**
     * Generate a multi-page A4 PDF containing a tabular listing of ID cards.
     *
     * @param profiles List of profiles
     * @return PDF as byte array
     */
    public static byte[] generateBatchIDCardsPDF(List<Profile> profiles)
            throws IOException, DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document doc = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Batch ID Cards Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(6);
        doc.add(title);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);
        Paragraph subtitle = new Paragraph("Total Records: " + profiles.size(), subFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(14);
        doc.add(subtitle);

        // Table
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{5f, 16f, 14f, 12f, 14f, 10f, 10f});

        addTableHeader(table, new String[]{"#", "Name", "Reg. Number", "Dept.", "Title/Class", "Type", "Issue Date"});

        int idx = 1;
        for (Profile p : profiles) {
            addBatchRow(table, idx++, p);
        }

        doc.add(table);
        doc.close();
        return baos.toByteArray();
    }

    // -------------------------------------------------------------------------
    // Card rendering
    // -------------------------------------------------------------------------

    private static void renderCard(PdfContentByte canvas, Document doc, Profile profile, byte[] photoBytes)
            throws DocumentException, IOException {

        Template tpl = profile.getTemplate();

        // Resolve colours
        BaseColor primary   = hexToBaseColor(tpl != null ? tpl.getPrimaryColor()   : "#1d4ed8");
        BaseColor secondary = hexToBaseColor(tpl != null ? tpl.getSecondaryColor() : "#dbeafe");
        BaseColor text      = hexToBaseColor(tpl != null ? tpl.getTextColor()      : "#1e3a5f");

        float w = CARD_W - 24;  // effective width (margins = 12 each side)

        // ── Header band ──────────────────────────────────────────────────────
        String orgName = (tpl != null && tpl.getOrganizationName() != null)
                ? tpl.getOrganizationName() : "ITC Institution";
        String tagline = (tpl != null && tpl.getTagline() != null)
                ? tpl.getTagline() : "";

        Font orgFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
        Font tagFont  = FontFactory.getFont(FontFactory.HELVETICA, 7, new BaseColor(220, 220, 220));
        Font typeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, BaseColor.WHITE);

        PdfPTable header = new PdfPTable(1);
        header.setTotalWidth(w);
        header.setLockedWidth(true);

        PdfPCell orgCell = new PdfPCell(new Phrase(orgName, orgFont));
        orgCell.setBackgroundColor(primary);
        orgCell.setBorder(Rectangle.NO_BORDER);
        orgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        orgCell.setPadding(8);
        orgCell.setPaddingBottom(2);
        header.addCell(orgCell);

        if (!tagline.isBlank()) {
            PdfPCell tagCell = new PdfPCell(new Phrase(tagline, tagFont));
            tagCell.setBackgroundColor(primary);
            tagCell.setBorder(Rectangle.NO_BORDER);
            tagCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tagCell.setPadding(0);
            tagCell.setPaddingBottom(6);
            header.addCell(tagCell);
        }

        String typeLabel = profile.getType() != null ? profile.getType().name() + " ID CARD" : "ID CARD";
        PdfPCell typeBadge = new PdfPCell(new Phrase(typeLabel, typeFont));
        typeBadge.setBackgroundColor(darken(primary, 30));
        typeBadge.setBorder(Rectangle.NO_BORDER);
        typeBadge.setHorizontalAlignment(Element.ALIGN_CENTER);
        typeBadge.setPaddingTop(3);
        typeBadge.setPaddingBottom(3);
        header.addCell(typeBadge);

        doc.add(header);
        doc.add(new Paragraph("\n"));

        // ── Photo + Info side-by-side ────────────────────────────────────────
        PdfPTable mainRow = new PdfPTable(2);
        mainRow.setTotalWidth(w);
        mainRow.setLockedWidth(true);
        mainRow.setWidths(new float[]{35f, 65f});

        // Photo cell
        PdfPCell photoCell;
        if (photoBytes != null && photoBytes.length > 0) {
            try {
                Image img = Image.getInstance(photoBytes);
                img.scaleToFit(90, 110);
                photoCell = new PdfPCell(img);
                photoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                photoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            } catch (Exception e) {
                photoCell = placeholderPhotoCell(secondary, text);
            }
        } else {
            photoCell = placeholderPhotoCell(secondary, text);
        }
        photoCell.setBorder(Rectangle.NO_BORDER);
        photoCell.setPadding(4);
        mainRow.addCell(photoCell);

        // Info cell
        PdfPTable infoTable = new PdfPTable(1);
        infoTable.setWidthPercentage(100);

        Font nameFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, text);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, primary);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 8, text);

        addInfoRow(infoTable, profile.getFullName(), nameFont, null, null);
        if (profile.getTitle() != null && !profile.getTitle().isBlank()) {
            addInfoRow(infoTable, profile.getTitle(), valueFont, null, null);
        }
        addDivider(infoTable, secondary);
        if (profile.getDepartment() != null) {
            addInfoRow(infoTable, "DEPT.", labelFont, profile.getDepartment(), valueFont);
        }
        if (profile.getEmail() != null) {
            addInfoRow(infoTable, "EMAIL", labelFont, profile.getEmail(), valueFont);
        }
        if (profile.getPhone() != null) {
            addInfoRow(infoTable, "PHONE", labelFont, profile.getPhone(), valueFont);
        }
        if (profile.getBloodGroup() != null) {
            addInfoRow(infoTable, "BLOOD", labelFont, profile.getBloodGroup(), valueFont);
        }
        if (profile.getDateOfBirth() != null) {
            addInfoRow(infoTable, "DOB", labelFont, profile.getDateOfBirth().format(DATE_FMT), valueFont);
        }

        PdfPCell infoCell = new PdfPCell(infoTable);
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPaddingLeft(4);
        mainRow.addCell(infoCell);

        doc.add(mainRow);
        doc.add(new Paragraph("\n"));

        // ── Registration + Expiry strip ───────────────────────────────────────
        PdfPTable strip = new PdfPTable(2);
        strip.setTotalWidth(w);
        strip.setLockedWidth(true);

        Font stripLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, BaseColor.WHITE);
        Font stripValueFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.WHITE);

        PdfPCell regCell = buildStripCell("ID: " + profile.getRegistrationNumber(), stripLabelFont, stripValueFont, primary);
        regCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        strip.addCell(regCell);

        String expiry = profile.getExpiryDate() != null
                ? "Valid until: " + profile.getExpiryDate().format(DATE_FMT)
                : "No Expiry";
        PdfPCell expCell = buildStripCell(expiry, stripLabelFont, stripValueFont, primary);
        expCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        strip.addCell(expCell);

        doc.add(strip);
        doc.add(new Paragraph("\n"));

        // ── QR Code + Barcode side-by-side ───────────────────────────────────
        PdfPTable codeRow = new PdfPTable(2);
        codeRow.setTotalWidth(w);
        codeRow.setLockedWidth(true);

        // QR Code
        try {
            String qrContent = "https://verify.idcard.local/" + profile.getUuid()
                    + "?reg=" + profile.getRegistrationNumber();
            byte[] qrBytes = QRCodeUtil.generateQRCodeBytes(qrContent);
            Image qrImg = Image.getInstance(qrBytes);
            qrImg.scaleToFit(80, 80);
            PdfPCell qrCell = new PdfPCell(qrImg);
            qrCell.setBorder(Rectangle.NO_BORDER);
            qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qrCell.setPadding(2);
            codeRow.addCell(qrCell);
        } catch (Exception e) {
            codeRow.addCell(emptyCell());
        }

        // Barcode (Code-128 or EAN)
        try {
            BarcodeType barcodeType = profile.getBarcodeType() != null
                    ? profile.getBarcodeType() : BarcodeType.CODE_128;
            String barcodeContent = profile.getRegistrationNumber();
            BufferedImage barcodeImg;
            if (barcodeType == BarcodeType.EAN_13) {
                barcodeImg = BarcodeUtil.generateEAN13Image(barcodeContent);
            } else {
                barcodeImg = BarcodeUtil.generateCode128Image(barcodeContent);
            }
            // Convert BufferedImage → iText Image
            ByteArrayOutputStream bcBaos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(barcodeImg, "PNG", bcBaos);
            Image bcImg = Image.getInstance(bcBaos.toByteArray());
            bcImg.scaleToFit(130, 50);
            PdfPCell bcCell = new PdfPCell(bcImg);
            bcCell.setBorder(Rectangle.NO_BORDER);
            bcCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            bcCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            bcCell.setPadding(4);
            codeRow.addCell(bcCell);
        } catch (Exception e) {
            codeRow.addCell(emptyCell());
        }

        doc.add(codeRow);

        // ── Footer ────────────────────────────────────────────────────────────
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.GRAY);
        Paragraph footer = new Paragraph(
                "This card is the property of " + orgName + ". If found, please return it.", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(4);
        doc.add(footer);
    }

    // -------------------------------------------------------------------------
    // Batch table helpers
    // -------------------------------------------------------------------------

    private static void addTableHeader(PdfPTable table, String[] headers) {
        Font hFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.WHITE);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, hFont));
            cell.setBackgroundColor(new BaseColor(29, 78, 216));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private static void addBatchRow(PdfPTable table, int idx, Profile p) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 8);
        BaseColor bg = (idx % 2 == 0) ? new BaseColor(241, 245, 249) : BaseColor.WHITE;

        addCell(table, String.valueOf(idx), f, bg, Element.ALIGN_CENTER);
        addCell(table, p.getFullName(), f, bg, Element.ALIGN_LEFT);
        addCell(table, nvl(p.getRegistrationNumber()), f, bg, Element.ALIGN_LEFT);
        addCell(table, nvl(p.getDepartment()), f, bg, Element.ALIGN_LEFT);
        addCell(table, nvl(p.getTitle()), f, bg, Element.ALIGN_LEFT);
        addCell(table, p.getType() != null ? p.getType().name() : "-", f, bg, Element.ALIGN_CENTER);
        addCell(table, p.getIssueDate() != null ? p.getIssueDate().format(DATE_FMT) : "-", f, bg, Element.ALIGN_CENTER);
    }

    private static void addCell(PdfPTable table, String text, Font font,
                                BaseColor bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setPadding(4);
        table.addCell(cell);
    }

    // -------------------------------------------------------------------------
    // Layout helpers
    // -------------------------------------------------------------------------

    private static void addInfoRow(PdfPTable t, String label, Font labelFont,
                                   String value, Font valueFont) {
        if (value == null) {
            // Full-width label only
            PdfPCell c = new PdfPCell(new Phrase(label, labelFont));
            c.setBorder(Rectangle.NO_BORDER);
            c.setColspan(1);
            c.setPaddingBottom(2);
            t.addCell(c);
        } else {
            PdfPCell c = new PdfPCell();
            c.setBorder(Rectangle.NO_BORDER);
            c.setPaddingBottom(2);
            Phrase combined = new Phrase();
            combined.add(new Chunk(label + "  ", labelFont));
            combined.add(new Chunk(value, valueFont));
            c.setPhrase(combined);
            t.addCell(c);
        }
    }

    private static void addDivider(PdfPTable t, BaseColor color) {
        PdfPCell div = new PdfPCell(new Phrase(" "));
        div.setBorder(Rectangle.BOTTOM);
        div.setBorderColor(color);
        div.setPaddingBottom(4);
        t.addCell(div);
    }

    private static PdfPCell placeholderPhotoCell(BaseColor bg, BaseColor text) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setFixedHeight(100);
        Paragraph p = new Paragraph("NO\nPHOTO",
                FontFactory.getFont(FontFactory.HELVETICA, 8, text));
        p.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(p);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private static PdfPCell buildStripCell(String text, Font labelFont, Font valueFont, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, valueFont));
        cell.setBackgroundColor(bg);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }

    private static PdfPCell emptyCell() {
        PdfPCell c = new PdfPCell(new Phrase(""));
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    // -------------------------------------------------------------------------
    // Colour helpers
    // -------------------------------------------------------------------------

    private static BaseColor hexToBaseColor(String hex) {
        try {
            String h = hex.replace("#", "");
            int rgb = Integer.parseInt(h, 16);
            return new BaseColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
        } catch (Exception e) {
            return BaseColor.BLACK;
        }
    }

    private static BaseColor darken(BaseColor c, int amount) {
        return new BaseColor(
                Math.max(0, c.getRed()   - amount),
                Math.max(0, c.getGreen() - amount),
                Math.max(0, c.getBlue()  - amount)
        );
    }

    private static String nvl(String s) {
        return s != null ? s : "-";
    }
}
