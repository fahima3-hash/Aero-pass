package com.aeropass.models;

public class Discount {
    private String code;
    private double percentage;
    private String description;

    public Discount(String code, double percentage, String description) {
        this.code = code;
        this.percentage = percentage;
        this.description = description;
    }

    // Getters
    public String getCode() { return code; }
    public double getPercentage() { return percentage; }
    public String getDescription() { return description; }
}