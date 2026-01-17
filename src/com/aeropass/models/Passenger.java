package com.aeropass.models;

public class Passenger {
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private String passportNid;
    private String seatNumber;

    public Passenger(String firstName, String lastName, String dateOfBirth,
                     String gender, String passportNid, String seatNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.passportNid = passportNid;
        this.seatNumber = seatNumber;
    }

    // Getters
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getPassportNid() { return passportNid; }
    public String getSeatNumber() { return seatNumber; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}