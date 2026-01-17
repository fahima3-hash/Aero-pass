package com.aeropass;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
//class declaration and variable part
public class MongoDBConnection {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
// method ta connect kora hoise
    public static boolean connect() {
        try {
            // Use your database name from MongoDB Compass
            String connectionString = "mongodb://localhost:27017/fahimatina933_db_user";
            mongoClient = MongoClients.create(connectionString);
            database = mongoClient.getDatabase("fahimatina933_db_user");
            System.out.println("Connected to MongoDB successfully!");
            initializeCollections();
            return true;
        } catch (Exception e) {
            System.err.println("MongoDB connection failed: " + e.getMessage());
            return false;
        }
    }
    // flight method save kora hoise ey part tuku dara
    public static void saveFlight(String from, String to, String route, int price, String duration, String type) {
        try {
            MongoCollection<Document> flightsCollection = database.getCollection("flights");

            Document flight = new Document()
                    .append("from", from)
                    .append("to", to)
                    .append("route", route)
                    .append("price", price)
                    .append("duration", duration)
                    .append("type", type)
                    .append("createdDate", new java.util.Date());

            flightsCollection.insertOne(flight);
            System.out.println("✓ Flight saved to MongoDB");

        } catch (Exception e) {
            System.err.println("Error saving flight to MongoDB: " + e.getMessage());
        }
    }
    //initialized connection part
    //contains() check kore j connection ti age o chiilo ba achee kina

    private static void initializeCollections() {
        //jdy collection na thke notun kore create kore
        List<String> collections = database.listCollectionNames().into(new ArrayList<>());

        if (!collections.contains("bookings")) {
            database.createCollection("bookings");
            System.out.println("Created 'bookings' collection");
        }
        if (!collections.contains("flights")) {
            database.createCollection("flights");
            System.out.println("Created 'flights' collection");
        }
        if (!collections.contains("users")) {
            database.createCollection("users");
            System.out.println("Created 'users' collection");
        }
    }
     // gate database method
    //database ta object return kore onno arek class a use korar jnno
    public static MongoDatabase getDatabase() {
        return database;
    }
    //close kora hche
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed");
        }
    }

    // MongoDB te booking ta save hche
    public static void saveBooking(String flightRoute, String seatClass, List<String> selectedSeats,
                                   String email, String phone, double totalPrice, String discountCode) {
        try {
            MongoCollection<Document> bookingsCollection = database.getCollection("bookings");

            Document booking = new Document()
                    .append("flightRoute", flightRoute)
                    .append("seatClass", seatClass)
                    .append("selectedSeats", selectedSeats)
                    .append("email", email)
                    .append("phone", phone)
                    .append("totalPrice", totalPrice)
                    .append("discountCode", discountCode)
                    .append("bookingDate", new java.util.Date())
                    .append("status", "confirmed");

            bookingsCollection.insertOne(booking);
            System.out.println("✓ Booking saved to MongoDB");

        } catch (Exception e) {
            System.err.println("Error saving booking to MongoDB: " + e.getMessage());
        }
    }

    // Get all bookings from MongoDB
    //getAllBookings(): সকল বুকিং ডাটা রিটার্ন করে
    //find(): সকল ডকুমেন্ট খোঁজে
    //into(bookings): রেজাল্টকে লিস্টে কনভার্ট করে
    //bookings.size(): কতগুলো বুকিং লোড হয়েছে তা দেখায়
    public static List<Document> getAllBookings() {
        List<Document> bookings = new ArrayList<>();
        try {
            MongoCollection<Document> bookingsCollection = database.getCollection("bookings");
            bookingsCollection.find().into(bookings);
            System.out.println("Loaded " + bookings.size() + " bookings from MongoDB");
        } catch (Exception e) {
            System.err.println("Error fetching bookings: " + e.getMessage());
        }
        return bookings;
    }
}