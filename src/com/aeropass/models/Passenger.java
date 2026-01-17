package com.aeropass.models;

public class Passenger {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String passport;
    private String flight;
    private String seat;
    private String bookingDate;

    public Passenger(String name, String email, String phone, String passport, String flight, String seat) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passport = passport;
        this.flight = flight;
        this.seat = seat;
        this.bookingDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassport() { return passport; }
    public void setPassport(String passport) { this.passport = passport; }

    public String getFlight() { return flight; }
    public void setFlight(String flight) { this.flight = flight; }

    public String getSeat() { return seat; }
    public void setSeat(String seat) { this.seat = seat; }

    public String getBookingDate() { return bookingDate; }

    @Override
    public String toString() {
        return "Passenger: " + name + " | Email: " + email + " | Flight: " + flight + " | Seat: " + seat;
    }
}