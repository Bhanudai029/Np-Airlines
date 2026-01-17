package com.npairlines.data.model;

import com.google.gson.annotations.SerializedName;

public class Flight {
    @SerializedName("id")
    private String id;
    
    @SerializedName("flight_number")
    private String flightNumber;
    
    @SerializedName("origin_code")
    private String originCode;
    
    @SerializedName("destination_code")
    private String destinationCode;
    
    @SerializedName("price_economy")
    private double priceEconomy;
    
    @SerializedName("departure_time")
    private String departureTime;
    
    @SerializedName("arrival_time")
    private String arrivalTime;
    
    @SerializedName("airline")
    private String airline;
    
    @SerializedName("duration_mins")
    private int durationMins;

    // Getters
    public String getId() { return id; }
    public String getFlightNumber() { return flightNumber; }
    public String getOriginCode() { return originCode; }
    public String getDestinationCode() { return destinationCode; }
    public double getPriceEconomy() { return priceEconomy; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getAirline() { return airline; }
    public int getDurationMins() { return durationMins; }
}
