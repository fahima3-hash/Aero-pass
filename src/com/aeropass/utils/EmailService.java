package com.aeropass.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class EmailService {

    // ===== CONFIGURE THESE FOR REAL EMAILS =====
    // এখানে মাত্র একবার declare করুন
    private static final String COMPANY_EMAIL = "aeropass.airlines@gmail.com"; // Your company email
    private static final String APP_PASSWORD = "your-16-digit-app-password";   // Google App Password

    /**
     * Main method to send ticket to customer
     */
    public static boolean sendTicketToCustomer(String customerEmail,
                                               String passengerName,
                                               String flightInfo,
                                               String seatInfo) {

        System.out.println("\n🎫 PROCESSING TICKET FOR: " + passengerName);
        System.out.println("📧 CUSTOMER EMAIL: " + customerEmail);

        // Generate booking reference
        String bookingRef = generateBookingReference();
        System.out.println("📋 BOOKING REF: " + bookingRef);

        // Create tickets folder
        File ticketsDir = new File("tickets");
        if (!ticketsDir.exists()) {
            ticketsDir.mkdir();
            System.out.println("📁 Created tickets folder");
        }

        // Generate PDF
        String pdfFileName = "Aeropass_Ticket_" + bookingRef + ".pdf";
        String pdfPath = ticketsDir.getAbsolutePath() + File.separator + pdfFileName;

        System.out.println("📄 Generating PDF ticket...");
        boolean pdfCreated = createProfessionalTicketPDF(passengerName, flightInfo, seatInfo, bookingRef, pdfPath);

        if (!pdfCreated) {
            System.out.println("❌ PDF generation failed!");
            return false;
        }

        System.out.println("✅ PDF generated: " + pdfPath);

        // Send email with PDF attachment
        boolean emailSent = sendEmailWithAttachment(customerEmail, passengerName, flightInfo, seatInfo, bookingRef, pdfPath);

        if (emailSent) {
            System.out.println("✅ Email sent successfully to: " + customerEmail);
            // Open PDF for local preview
            openPDF(pdfPath);
        } else {
            System.out.println("⚠️ Email not sent. Check email configuration.");
            System.out.println("📄 PDF saved locally at: " + pdfPath);
            openPDF(pdfPath);
        }

        return emailSent;
    }

    /**
     * Create professional PDF ticket
     */
    private static boolean createProfessionalTicketPDF(String passengerName, String flightInfo,
                                                       String seatInfo, String bookingRef,
                                                       String pdfPath) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfPath));

            document.open();

            // ===== HEADER =====
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, BaseColor.BLUE);
            Paragraph title = new Paragraph("✈️ AEROPASS AIRLINES", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph subtitle = new Paragraph("BOARDING PASS & E-TICKET", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(30);
            document.add(subtitle);

            // ===== BOOKING REFERENCE =====
            Font refFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.RED);
            Paragraph refPara = new Paragraph("BOOKING REFERENCE: " + bookingRef, refFont);
            refPara.setAlignment(Element.ALIGN_CENTER);
            refPara.setSpacingAfter(40);
            document.add(refPara);

            // ===== PASSENGER INFO TABLE =====
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(20);
            table.setSpacingAfter(30);

            // Table headers
            addTableCell(table, "FIELD", true, BaseColor.LIGHT_GRAY);
            addTableCell(table, "DETAILS", true, BaseColor.LIGHT_GRAY);

            // Passenger data
            addTableCell(table, "PASSENGER NAME", false, BaseColor.WHITE);
            addTableCell(table, passengerName.toUpperCase(), false, BaseColor.WHITE);

            addTableCell(table, "EMAIL", false, BaseColor.WHITE);
            addTableCell(table, getEmailFromSystem(), false, BaseColor.WHITE);

            addTableCell(table, "BOOKING DATE", false, BaseColor.WHITE);
            addTableCell(table, new SimpleDateFormat("dd MMMM yyyy").format(new Date()), false, BaseColor.WHITE);

            addTableCell(table, "BOOKING TIME", false, BaseColor.WHITE);
            addTableCell(table, new SimpleDateFormat("hh:mm a").format(new Date()), false, BaseColor.WHITE);

            document.add(table);

            // ===== FLIGHT DETAILS =====
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(0, 102, 204));
            Paragraph flightSection = new Paragraph("FLIGHT INFORMATION", sectionFont);
            flightSection.setSpacingAfter(15);
            document.add(flightSection);

            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 14);
            document.add(new Paragraph("• Flight Route: " + flightInfo, normalFont));
            document.add(new Paragraph("• Seat Number: " + seatInfo, normalFont));

            // Flight times
            String departureTime = new SimpleDateFormat("dd MMM yyyy, HH:mm").format(new Date());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, getFlightDuration(flightInfo));
            String arrivalTime = new SimpleDateFormat("dd MMM yyyy, HH:mm").format(cal.getTime());

            document.add(new Paragraph("• Departure: " + departureTime, normalFont));
            document.add(new Paragraph("• Arrival: " + arrivalTime, normalFont));
            document.add(new Paragraph("• Class: ECONOMY", normalFont));

            // Status
            Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.GREEN);
            document.add(new Paragraph("\nSTATUS: CONFIRMED ✓", statusFont));

            // ===== BARCODE =====
            document.add(new Paragraph("\n\n"));
            Font barcodeFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 20);
            Paragraph barcode = new Paragraph("|||| |||| | |||| || |||| | |||| ||", barcodeFont);
            barcode.setAlignment(Element.ALIGN_CENTER);
            document.add(barcode);

            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph barcodeText = new Paragraph("Scan at airport: " + bookingRef, smallFont);
            barcodeText.setAlignment(Element.ALIGN_CENTER);
            document.add(barcodeText);

            // ===== INSTRUCTIONS =====
            document.add(new Paragraph("\n\n"));
            Font instrFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.RED);
            Paragraph instructions = new Paragraph("IMPORTANT INSTRUCTIONS", instrFont);
            instructions.setSpacingAfter(10);
            document.add(instructions);

            Font bulletFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("✓ Online check-in opens 24 hours before departure", bulletFont));
            document.add(new Paragraph("✓ Arrive at airport minimum 3 hours before departure", bulletFont));
            document.add(new Paragraph("✓ Carry original Passport/NID & this ticket", bulletFont));
            document.add(new Paragraph("✓ Check-in counter closes 45 minutes before departure", bulletFont));
            document.add(new Paragraph("✓ Boarding gate closes 20 minutes before departure", bulletFont));

            // ===== CONTACT INFO =====
            document.add(new Paragraph("\n\n"));
            Font contactFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, BaseColor.GRAY);
            Paragraph contact = new Paragraph("For assistance: +880 1711-123456 | support@aeropass.com | www.aeropass.com", contactFont);
            contact.setAlignment(Element.ALIGN_CENTER);
            document.add(contact);

            document.close();
            return true;

        } catch (Exception e) {
            System.err.println("PDF Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send email with PDF attachment
     */
    private static boolean sendEmailWithAttachment(String toEmail, String passengerName,
                                                   String flightInfo, String seatInfo,
                                                   String bookingRef, String pdfPath) {

        // Check if real email credentials are configured
        if (COMPANY_EMAIL.equals("aeropass.airlines@gmail.com") ||
                APP_PASSWORD.equals("your-16-digit-app-password")) {
            System.out.println("⚠️ Email credentials not configured. Running in mock mode.");
            return false;
        }

        try {
            // Email properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

            // Create session
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(COMPANY_EMAIL, APP_PASSWORD);
                }
            });

            // Create message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(COMPANY_EMAIL, "Aeropass Airlines"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("✈️ Your Flight Ticket - Booking #" + bookingRef + " - Aeropass Airlines");

            // Create multipart
            MimeMultipart multipart = new MimeMultipart();

            // HTML email body
            MimeBodyPart htmlPart = new MimeBodyPart();
            String htmlContent = createEmailHTML(passengerName, flightInfo, seatInfo, bookingRef);
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // PDF attachment
            MimeBodyPart attachmentPart = new MimeBodyPart();
            FileDataSource source = new FileDataSource(pdfPath);
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName("Aeropass_Ticket_" + bookingRef + ".pdf");
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            // Send email
            Transport.send(message);
            return true;

        } catch (Exception e) {
            System.err.println("Email Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * HTML email template
     */
    private static String createEmailHTML(String passengerName, String flightInfo,
                                          String seatInfo, String bookingRef) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; }" +
                ".container { max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; }" +
                ".header { background: linear-gradient(135deg, #1a73e8, #0d47a1); color: white; padding: 25px; text-align: center; border-radius: 10px 10px 0 0; }" +
                ".content { padding: 20px; }" +
                ".summary { background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #1a73e8; }" +
                ".instructions { background: #fff3cd; padding: 15px; border-radius: 5px; border: 1px solid #ffeaa7; }" +
                ".footer { text-align: center; color: #666; font-size: 13px; margin-top: 25px; padding-top: 20px; border-top: 1px solid #eee; }" +
                "</style></head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>✈️ AEROPASS AIRLINES</h1>" +
                "<h2>Your Flight Ticket is Ready!</h2>" +
                "<p>Booking Reference: <strong style='background: white; color: #1a73e8; padding: 5px 10px; border-radius: 5px;'>" + bookingRef + "</strong></p>" +
                "</div>" +
                "<div class='content'>" +
                "<h3>Dear " + passengerName + ",</h3>" +
                "<p>Thank you for booking with Aeropass Airlines! Your flight ticket has been confirmed and is attached to this email.</p>" +
                "<div class='summary'>" +
                "<h4 style='color: #1a73e8;'>📋 Booking Summary</h4>" +
                "<p><strong>Passenger:</strong> " + passengerName + "</p>" +
                "<p><strong>Flight:</strong> " + flightInfo + "</p>" +
                "<p><strong>Seat:</strong> " + seatInfo + "</p>" +
                "<p><strong>Booking Reference:</strong> " + bookingRef + "</p>" +
                "</div>" +
                "<div class='instructions'>" +
                "<h4>📝 Important Instructions</h4>" +
                "<ul>" +
                "<li>Check-in online 24 hours before departure</li>" +
                "<li>Arrive at airport 3 hours before departure</li>" +
                "<li>Carry valid Passport/NID & this ticket</li>" +
                "<li>Check-in closes 45 minutes before departure</li>" +
                "<li>Boarding starts 40 minutes before departure</li>" +
                "</ul>" +
                "</div>" +
                "<p style='text-align: center; background: #e7f3ff; padding: 15px; border-radius: 5px;'>" +
                "<strong>📎 Your ticket is attached as a PDF file.</strong><br>" +
                "Print it or show on your mobile at airport" +
                "</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>📞 <strong>24/7 Customer Support:</strong> +880 1711-123456</p>" +
                "<p>📧 <strong>Email:</strong> support@aeropass.com</p>" +
                "<p>🌐 <strong>Website:</strong> www.aeropass.com</p>" +
                "<p style='color: #999; margin-top: 15px; font-size: 12px;'>This is an automated email. Please do not reply.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Helper methods
     */
    /**
     * Helper methods
     */
    private static void addTableCell(PdfPTable table, String text, boolean isHeader, BaseColor bgColor) {
        // Create Phrase with appropriate font
        Phrase phrase;
        if (isHeader) {
            phrase = new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
        } else {
            phrase = new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 12));
        }

        PdfPCell cell = new PdfPCell(phrase);
        cell.setPadding(10);
        cell.setBackgroundColor(bgColor);
        if (isHeader) {
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        }
        table.addCell(cell);
    }

    private static String getEmailFromSystem() {
        return "customer@aeropass.com"; // Default email
    }

    private static int getFlightDuration(String flightInfo) {
        if (flightInfo.contains("DXB")) return 6;
        if (flightInfo.contains("JFK")) return 14;
        if (flightInfo.contains("LHR")) return 11;
        return 6; // default
    }

    private static String generateBookingReference() {
        Random rand = new Random();
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        sb.append("AP");
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static void openPDF(String pdfPath) {
        try {
            File pdfFile = new File(pdfPath);
            if (pdfFile.exists()) {
                java.awt.Desktop.getDesktop().open(pdfFile);
                System.out.println("👁️ PDF opened for preview");
            }
        } catch (Exception e) {
            System.out.println("📁 PDF saved at: " + pdfPath);
        }
    }

    /**
     * Test method
     */
    public static void main(String[] args) {
        System.out.println("=== Email Service Test ===");

        boolean success = sendTicketToCustomer(
                "customer@gmail.com", // Replace with real customer email
                "John Smith",
                "DAC (Dhaka) → DXB (Dubai) | EK585 | Economy",
                "14A (Window)"
        );

        if (success) {
            System.out.println("\n✅ Ticket sent successfully!");
        }
    }
}