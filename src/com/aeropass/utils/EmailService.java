package com.aeropass.utils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class EmailService {

    // এখানে আপনার REAL Gmail credentials দিন
    private static final String FROM_EMAIL = "your.email@gmail.com"; // আপনার জিমেইল
    private static final String FROM_PASSWORD = "your-app-password"; // Google App Password

    /**
     * Customer-কে Ticket পাঠানোর Main Method
     */
    public static boolean sendTicketToCustomer(String customerEmail,
                                               String passengerName,
                                               String flightInfo,
                                               String seatNumber) {

        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚀 STARTING TICKET DELIVERY PROCESS");
        System.out.println("=".repeat(60));

        // 1. Generate Booking Reference
        String bookingRef = generateBookingReference();
        System.out.println("📋 Generated Booking Ref: " + bookingRef);

        // 2. Parse flight information
        String[] flightParts = parseFlightInfo(flightInfo);
        String flightNo = flightParts[0];
        String departure = flightParts[1];
        String destination = flightParts[2];

        // 3. Create tickets folder if not exists
        File ticketsDir = new File("tickets");
        if (!ticketsDir.exists()) {
            ticketsDir.mkdir();
            System.out.println("📁 Created tickets folder");
        }

        // 4. Generate PDF
        String pdfFileName = "ticket_" + bookingRef + ".pdf";
        String pdfPath = ticketsDir.getAbsolutePath() + File.separator + pdfFileName;

        System.out.println("📄 Generating PDF Ticket...");

        String departureTime = new SimpleDateFormat("dd MMM yyyy, HH:mm").format(new Date());

        // Add 6 hours for arrival
        Date arrivalDate = new Date(System.currentTimeMillis() + (6 * 60 * 60 * 1000));
        String arrivalTime = new SimpleDateFormat("dd MMM yyyy, HH:mm").format(arrivalDate);

        String pdfResult = PDFGenerator.generateTicketPDF(
                passengerName.toUpperCase(),
                flightNo,
                departure,
                destination,
                departureTime,
                arrivalTime,
                seatNumber,
                bookingRef,
                pdfPath
        );

        if (pdfResult == null) {
            System.out.println("❌ PDF Generation FAILED!");
            return false;
        }

        System.out.println("✅ PDF Generated: " + pdfPath);

        // 5. Check if PDF file actually created
        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists()) {
            System.out.println("❌ PDF file not found at: " + pdfPath);
            return false;
        }

        System.out.println("📊 PDF Size: " + pdfFile.length() + " bytes");

        // 6. Send Email with PDF
        boolean emailSent = sendEmailWithAttachment(customerEmail, passengerName,
                flightInfo, seatNumber, bookingRef, pdfPath);

        // 7. Show result
        System.out.println("\n" + "=".repeat(60));
        if (emailSent) {
            System.out.println("🎉 SUCCESS! Ticket delivered to customer");
            System.out.println("📧 Email sent to: " + customerEmail);
            System.out.println("📎 PDF attached: " + pdfFileName);
        } else {
            System.out.println("⚠️ Ticket saved locally but email not sent");
            System.out.println("📄 PDF saved at: " + pdfPath);
            System.out.println("🔧 Please configure email credentials");
        }
        System.out.println("=".repeat(60));

        // 8. Open PDF for local viewing
        try {
            if (pdfFile.exists()) {
                java.awt.Desktop.getDesktop().open(pdfFile);
                System.out.println("👁️ PDF opened for preview");
            }
        } catch (Exception e) {
            // Ignore if can't open
        }

        return emailSent;
    }

    /**
     * Email পাঠানোর Method
     */
    private static boolean sendEmailWithAttachment(String toEmail, String passengerName,
                                                   String flightInfo, String seatNumber,
                                                   String bookingRef, String pdfPath) {

        // Check if using real credentials
        if (FROM_EMAIL.equals("your.email@gmail.com") || FROM_PASSWORD.equals("your-app-password")) {
            System.out.println("⚠️ Running in MOCK MODE - No real email sent");
            System.out.println("📧 Would send to: " + toEmail);
            return false; // Mock mode
        }

        System.out.println("✉️ Preparing to send REAL email...");

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
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        try {
            // Create message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, "Aeropass Airlines"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("✈️ Your Flight Ticket - Booking #" + bookingRef);

            // Create multipart
            MimeMultipart multipart = new MimeMultipart();

            // Text part
            MimeBodyPart textPart = new MimeBodyPart();
            String emailText = createEmailText(passengerName, flightInfo, seatNumber, bookingRef);
            textPart.setText(emailText);
            multipart.addBodyPart(textPart);

            // HTML part
            MimeBodyPart htmlPart = new MimeBodyPart();
            String htmlContent = createEmailHTML(passengerName, flightInfo, seatNumber, bookingRef);
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // Attachment part (PDF)
            MimeBodyPart attachmentPart = new MimeBodyPart();
            FileDataSource source = new FileDataSource(pdfPath);
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName("Aeropass_Ticket_" + bookingRef + ".pdf");
            multipart.addBodyPart(attachmentPart);

            // Set content
            message.setContent(multipart);

            // Send email
            Transport.send(message);

            System.out.println("✅ Email SENT successfully to: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Email sending failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Simple email text
     */
    private static String createEmailText(String name, String flight, String seat, String ref) {
        return "Dear " + name + ",\n\n" +
                "Thank you for booking with Aeropass Airlines!\n\n" +
                "Your flight details:\n" +
                "Flight: " + flight + "\n" +
                "Seat: " + seat + "\n" +
                "Booking Reference: " + ref + "\n\n" +
                "Your e-ticket is attached as a PDF file.\n\n" +
                "Please arrive at airport 3 hours before departure.\n" +
                "Check-in closes 45 minutes before departure.\n\n" +
                "For assistance: +880 1711-123456\n" +
                "support@aeropass.com\n\n" +
                "Safe travels!\n" +
                "Aeropass Airlines";
    }

    /**
     * HTML email content
     */
    private static String createEmailHTML(String name, String flight, String seat, String ref) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd;'>" +
                "<div style='background: #1a73e8; color: white; padding: 20px; text-align: center;'>" +
                "<h1>✈️ AEROPASS AIRLINES</h1>" +
                "<h2>Flight Ticket Confirmation</h2>" +
                "</div>" +
                "<div style='padding: 20px;'>" +
                "<h3>Dear " + name + ",</h3>" +
                "<p>Your flight booking is confirmed! Your e-ticket is attached.</p>" +
                "<div style='background: #f5f5f5; padding: 15px; margin: 20px 0;'>" +
                "<p><strong>Flight:</strong> " + flight + "</p>" +
                "<p><strong>Seat:</strong> " + seat + "</p>" +
                "<p><strong>Booking Reference:</strong> " + ref + "</p>" +
                "</div>" +
                "<p>Please print the attached PDF or show on mobile at airport.</p>" +
                "<p>Arrive 3 hours before departure. Check-in closes 45 minutes before departure.</p>" +
                "</div>" +
                "<div style='text-align: center; padding: 15px; background: #f0f0f0;'>" +
                "<p>📞 +880 1711-123456 | 📧 support@aeropass.com</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Generate booking reference
     */
    public static String generateBookingReference() {
        Random rand = new Random();
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Removed confusing characters
        StringBuilder sb = new StringBuilder();
        sb.append("AP");
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Parse flight information
     */
    private static String[] parseFlightInfo(String flightInfo) {
        String[] result = new String[3];
        result[0] = "BG101"; // Default flight number
        result[1] = "DHAKA (DAC)";
        result[2] = "DUBAI (DXB)";

        try {
            if (flightInfo.contains("→")) {
                String[] parts = flightInfo.split("→");
                if (parts.length >= 2) {
                    result[1] = parts[0].trim();
                    result[2] = parts[1].trim();

                    // Extract flight number if exists
                    if (flightInfo.contains("|")) {
                        String[] subParts = flightInfo.split("\\|");
                        if (subParts.length >= 2) {
                            result[0] = subParts[1].trim();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Use defaults
        }

        return result;
    }

    /**
     * TEST Method - সরাসরি Run করুন
     */
    public static void main(String[] args) {
        System.out.println("✈️ AEROPASS TICKET DELIVERY SYSTEM");
        System.out.println("Version 1.0 - Working Solution");
        System.out.println("\nThis will:");
        System.out.println("1. ✅ Generate PDF ticket");
        System.out.println("2. ✅ Save PDF in 'tickets' folder");
        System.out.println("3. ✅ Send email with attachment");
        System.out.println("4. ✅ Open PDF for preview");

        // Test data - এখানে Customer-এর real email দিন
        String customerEmail = "customer.email@gmail.com"; // ← CHANGE THIS

        boolean success = sendTicketToCustomer(
                customerEmail,
                "MD RAHMAN",
                "DHAKA (DAC) → DUBAI (DXB) | BG101 | ECONOMY",
                "14F (Window Seat)"
        );

        if (success) {
            System.out.println("\n🎉 EVERYTHING WORKED PERFECTLY!");
        } else {
            System.out.println("\n⚠️ Email not sent (running in mock mode)");
            System.out.println("🔧 To enable real emails:");
            System.out.println("   1. Change FROM_EMAIL in EmailService.java");
            System.out.println("   2. Get Google App Password");
            System.out.println("   3. Change FROM_PASSWORD");
            System.out.println("   4. Run again");
        }

        System.out.println("\n📁 Check 'tickets' folder for generated PDF");
    }
}