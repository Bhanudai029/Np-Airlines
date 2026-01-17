package com.npairlines.data.repository;

import com.google.gson.JsonObject;
import com.npairlines.data.SupabaseClient;
import com.npairlines.data.service.SupabaseService;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingRepository {
    private final SupabaseService service;

    public BookingRepository() {
        this.service = SupabaseClient.getInstance().getService();
    }

    public interface BookingCallback {
        void onSuccess(String bookingReference);
        void onError(String message);
    }

    public void createBooking(String userId, String flightId, String seatId, double price, final BookingCallback callback) {
        // 1. Create Booking Record
        JsonObject booking = new JsonObject();
        booking.addProperty("user_id", userId);
        booking.addProperty("flight_id", flightId);
        String ref = "NP" + System.currentTimeMillis();
        booking.addProperty("booking_reference", ref);
        booking.addProperty("total_price", price);
        booking.addProperty("status", "CONFIRMED");

        service.insert("bookings", booking, "return=representation").enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Update Seat to BOOKED
                    updateSeatStatus(seatId, callback, ref);
                } else {
                    callback.onError("Booking creation failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
    
    private void updateSeatStatus(String seatId, final BookingCallback callback, final String ref) {
        // Handle multiple seat IDs (comma separated)
        String[] ids = seatId.split(",");
        
        // Use RPC or loop? Loop is easier with existing service methods without creating new RPC
        // Or better: update seats where id in (list)
        
        JsonObject update = new JsonObject();
        update.addProperty("status", "BOOKED");
        
        // Ideally we use "in" filter, but service.update takes filters map.
        // Let's assume generic update works with query params.
        
        // Simplified approach: Update one by one (reliable) or construct filter
        updateSeatsRecursive(ids, 0, update, callback, ref);
    }
    
    private void updateSeatsRecursive(final String[] ids, final int index, final JsonObject update, final BookingCallback callback, final String ref) {
         if (index >= ids.length) {
             callback.onSuccess(ref);
             return;
         }
         
         String currentId = ids[index].trim();
         java.util.Map<String, String> filters = new java.util.HashMap<>();
         filters.put("id", "eq." + currentId);
         
         service.update("seats", filters, update).enqueue(new Callback<Void>() {
             @Override
             public void onResponse(Call<Void> call, Response<Void> response) {
                 // Continue even if one fails? Better to try all.
                 updateSeatsRecursive(ids, index + 1, update, callback, ref);
             }
 
             @Override
             public void onFailure(Call<Void> call, Throwable t) {
                 updateSeatsRecursive(ids, index + 1, update, callback, ref);
             }
         });
    }

    public void getUserBookings(String userId, final BookingsCallback callback) {
        java.util.Map<String, String> filters = new java.util.HashMap<>();
        filters.put("user_id", "eq." + userId);
        filters.put("order", "created_at.desc");

        service.getTable("bookings", filters, "0-50").enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.List<com.npairlines.data.model.Booking>>(){}.getType();
                        java.util.List<com.npairlines.data.model.Booking> bookings = new com.google.gson.Gson().fromJson(json, listType);
                        callback.onSuccess(bookings);
                    } catch (java.io.IOException e) {
                        callback.onError("Parse error");
                    }
                } else {
                    callback.onError("Fetch failed");
                }
            }
            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface BookingsCallback {
        void onSuccess(java.util.List<com.npairlines.data.model.Booking> bookings);
        void onError(String message);
    }
}
