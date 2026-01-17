package com.aeropass.models;

public class Flight {
    private String route;
    private String stops;
    private String duration;
    private String price;
    private String type;
    private int priceValue;
    private boolean isCheapest;

    public Flight(String route, String stops, String duration, String price, String type, int priceValue) {
        this.route = route;
        this.stops = stops;
        this.duration = duration;
        this.price = price;
        this.type = type;
        this.priceValue = priceValue;
        this.isCheapest = false;
    }

    // Getters and Setters
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getStops() { return stops; }
    public void setStops(String stops) { this.stops = stops; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getPriceValue() { return priceValue; }
    public void setPriceValue(int priceValue) { this.priceValue = priceValue; }

    public boolean isCheapest() { return isCheapest; }
    public void setCheapest(boolean cheapest) { isCheapest = cheapest; }
}