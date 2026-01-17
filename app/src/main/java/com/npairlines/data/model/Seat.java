package com.npairlines.data.model;

import com.google.gson.annotations.SerializedName;

public class Seat {
    @SerializedName("id")
    private String id;
    
    @SerializedName("seat_number")
    private String seatNumber;

    @SerializedName("class")
    private String seatClass; // ECONOMY, BUSINESS, FIRST

    @SerializedName("status")
    private String status; // AVAILABLE, LOCKED, BOOKED
    
    public String getId() { return id; }
    public String getSeatNumber() { return seatNumber; }
    public String getSeatClass() { return seatClass; }
    public String getStatus() { return status; }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
