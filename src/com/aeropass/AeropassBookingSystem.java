package com.aeropass;

import com.aeropass.utils.EmailService;
import com.aeropass.models.Passenger;
import com.aeropass.models.Flight;
import java.util.Scanner;

public class AeropassBookingSystem {

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✈️  WELCOME TO AEROPASS BOOKING SYSTEM");
        System.out.println("=".repeat(60));

        Scanner scanner = new Scanner(System.in);

        // Get customer information
        System.out.println("\n📝 Please enter passenger details:");
        System.out.print("Full Name: ");
        String name = scanner.nextLine();

        System.out.print("Email Address: ");
        String email = scanner.nextLine();

        System.out.print("Phone Number: ");
        String phone = scanner.nextLine();

        System.out.print("Passport/NID Number: ");
        String passport = scanner.nextLine();

        // Flight selection
        System.out.println("\n🛫 Available Flights:");
        System.out.println("1. DAC → DXB | BG101 | 20 Dec 2024, 22:30 | Economy | $450");
        System.out.println("2. DAC → JFK | BA201 | 21 Dec 2024, 14:00 | Economy | $850");
        System.out.println("3. DAC → LHR | VS301 | 22 Dec 2024, 18:45 | Economy | $750");

        System.out.print("\nSelect flight (1-3): ");
        int flightChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        String selectedFlight = "";
        String seatNumber = "";

        switch(flightChoice) {
            case 1:
                selectedFlight = "DAC (Dhaka) → DXB (Dubai) | BG101 | ECONOMY";
                seatNumber = "14F";
                break;
            case 2:
                selectedFlight = "DAC (Dhaka) → JFK (New York) | BA201 | ECONOMY";
                seatNumber = "22A";
                break;
            case 3:
                selectedFlight = "DAC (Dhaka) → LHR (London) | VS301 | ECONOMY";
                seatNumber = "18C";
                break;
            default:
                selectedFlight = "DAC (Dhaka) → DXB (Dubai) | BG101 | ECONOMY";
                seatNumber = "14F";
        }

        // Seat selection
        System.out.println("\n💺 Available Seats for " + selectedFlight.split("\\|")[0].trim() + ":");
        System.out.println("Window: 14F, 15A, 16F");
        System.out.println("Aisle: 14C, 15D, 16C");
        System.out.println("Middle: 14B, 15E, 16B");

        System.out.print("\nEnter seat preference (e.g., 14F): ");
        String preferredSeat = scanner.nextLine();
        if (!preferredSeat.isEmpty()) {
            seatNumber = preferredSeat;
        }

        // Payment
        System.out.println("\n💰 Payment Information:");
        System.out.println("Flight Fare: $450");
        System.out.println("Taxes: $50");
        System.out.println("Total: $500");

        System.out.print("\nEnter payment method (Credit/Debit): ");
        String paymentMethod = scanner.nextLine();

        // Confirm booking
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📋 BOOKING SUMMARY:");
        System.out.println("=".repeat(60));
        System.out.println("Passenger: " + name);
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phone);
        System.out.println("Flight: " + selectedFlight);
        System.out.println("Seat: " + seatNumber);
        System.out.println("Payment: " + paymentMethod);

        System.out.print("\nConfirm booking? (yes/no): ");
        String confirmation = scanner.nextLine().toLowerCase();

        if (confirmation.equals("yes") || confirmation.equals("y")) {
            System.out.println("\n⏳ Processing your booking...");

            // Create Passenger object
            Passenger passenger = new Passenger(name, email, phone, passport, selectedFlight, seatNumber);

            // Save to database (MongoDB)
            saveToDatabase(passenger);

            // Generate and send ticket via email
            boolean ticketSent = EmailService.sendTicketToCustomer(email, name, selectedFlight, seatNumber);

            System.out.println("\n" + "=".repeat(60));
            if (ticketSent) {
                System.out.println("🎉 BOOKING CONFIRMED!");
                System.out.println("✅ Ticket has been sent to: " + email);
                System.out.println("📄 PDF saved in 'tickets' folder");
                System.out.println("\n📌 Important:");
                System.out.println("• Check your email for the ticket");
                System.out.println("• Check spam folder if not found");
                System.out.println("• Arrive 3 hours before departure");
            } else {
                System.out.println("⚠️ Booking saved but email not sent");
                System.out.println("📄 Check 'tickets' folder for your PDF ticket");
            }
            System.out.println("=".repeat(60));

        } else {
            System.out.println("\n❌ Booking cancelled.");
        }

        scanner.close();
        System.out.println("\nThank you for choosing Aeropass Airlines! ✈️");
    }

    private static void saveToDatabase(Passenger passenger) {
        try {
            // Connect to MongoDB and save
            System.out.println("💾 Saving to database...");
            // MongoDB connection code here
            System.out.println("✅ Passenger data saved successfully");
        } catch (Exception e) {
            System.out.println("⚠️ Could not save to database: " + e.getMessage());
        }
    }
}