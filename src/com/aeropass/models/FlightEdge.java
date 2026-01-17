package com.aeropass.models;

public class FlightEdge {
    private String destination;
    private int price;
    private String duration;

    public FlightEdge(String destination, int price, String duration) {
        this.destination = destination;
        this.price = price;
        this.duration = duration;
    }

    // Getters
    public String getDestination() { return destination; }
    public int getPrice() { return price; }
    public String getDuration() { return duration; }
}