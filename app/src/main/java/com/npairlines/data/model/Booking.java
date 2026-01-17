package com.npairlines.data.model;

import com.google.gson.annotations.SerializedName;

public class Booking {
    @SerializedName("id")
    private String id;
    
    @SerializedName("booking_reference")
    private String bookingReference;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("total_price")
    private double totalPrice;
    
    @SerializedName("created_at")
    private String createdAt;

    public String getBookingReference() { return bookingReference; }
    public String getStatus() { return status; }
    public double getTotalPrice() { return totalPrice; }
    public String getCreatedAt() { return createdAt; }
}
