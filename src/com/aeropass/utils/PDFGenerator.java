package com.aeropass;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDFGenerator {

    public static void generateTicket() {
        System.out.println("Starting PDF Generation...");

        try {
            // 1. Create tickets folder if not exists
            File ticketsDir = new File("tickets");
            if (!ticketsDir.exists()) {
                boolean created = ticketsDir.mkdir();
                System.out.println("Tickets folder created: " + created);
            }

            // 2. Create PDF file path
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String pdfName = "ticket_" + timestamp + ".pdf";
            String pdfPath = ticketsDir.getAbsolutePath() + File.separator + pdfName;

            System.out.println("Creating PDF: " + pdfPath);

            // 3. Create Document
            Document document = new Document();

            // 4. Create PdfWriter
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfPath));

            // 5. Open document
            document.open();

            // 6. Add content
            // Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.BLUE);
            Paragraph title = new Paragraph("✈️ AEROPASS AIRLINES", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Subtitle
            Paragraph subtitle = new Paragraph("ELECTRONIC TICKET RECEIPT",
                    new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(30);
            document.add(subtitle);

            // Passenger Info
            Paragraph passenger = new Paragraph("Passenger: MD RAHMAN");
            passenger.setSpacingAfter(10);
            document.add(passenger);

            Paragraph flight = new Paragraph("Flight: DAC (Dhaka) → DXB (Dubai)");
            flight.setSpacingAfter(10);
            document.add(flight);

            Paragraph seat = new Paragraph("Seat: 14F (Window)");
            seat.setSpacingAfter(10);
            document.add(seat);

            Paragraph date = new Paragraph("Date: " + new SimpleDateFormat("dd MMMM yyyy").format(new Date()));
            date.setSpacingAfter(10);
            document.add(date);

            Paragraph time = new Paragraph("Time: " + new SimpleDateFormat("hh:mm a").format(new Date()));
            time.setSpacingAfter(20);
            document.add(time);

            // Thank you message
            Paragraph thanks = new Paragraph("Thank you for choosing Aeropass Airlines!");
            thanks.setAlignment(Element.ALIGN_CENTER);
            thanks.setSpacingAfter(10);
            document.add(thanks);

            Paragraph instruction = new Paragraph("Please present this ticket at check-in counter");
            instruction.setAlignment(Element.ALIGN_CENTER);
            instruction.setFont(new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC));
            document.add(instruction);

            // 7. Close document
            document.close();

            System.out.println("✅ PDF Generated Successfully!");
            System.out.println("📄 File: " + pdfPath);

            // 8. Try to open PDF automatically
            try {
                File pdfFile = new File(pdfPath);
                if (pdfFile.exists()) {
                    java.awt.Desktop.getDesktop().open(pdfFile);
                    System.out.println("👁️ PDF opened automatically");
                }
            } catch (Exception e) {
                System.out.println("📁 Manually open: " + pdfPath);
            }

        } catch (Exception e) {
            System.err.println("❌ Error creating PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== PDF Generator Test ===");
        generateTicket();
    }
}