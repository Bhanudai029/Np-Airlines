package com.npairlines.data.repository;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.npairlines.data.SupabaseClient;
import com.npairlines.data.model.Seat;
import com.npairlines.data.service.SupabaseService;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeatRepository {
    private final SupabaseService service;
    private final Gson gson;

    public SeatRepository() {
        this.service = SupabaseClient.getInstance().getService();
        this.gson = new Gson();
    }

    public void getSeats(String flightId, final SeatCallback callback) {
        Map<String, String> filters = new HashMap<>();
        filters.put("flight_id", "eq." + flightId);
        filters.put("order", "seat_number.asc");

        service.getTable("seats", filters, "0-200").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        Type listType = new TypeToken<List<Seat>>(){}.getType();
                        List<Seat> seats = gson.fromJson(json, listType);
                        callback.onSuccess(seats);
                    } catch (IOException e) {
                        callback.onError("Parse error");
                    }
                } else {
                    callback.onError("Error fetching seats");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Call Postgres function to lock seat atomically (prevent double booking)
    public void lockSeat(String flightId, String seatNumber, String userId, final ActionCallback callback) {
        JsonObject body = new JsonObject();
        body.addProperty("p_flight_id", flightId);
        body.addProperty("p_seat_number", seatNumber);
        body.addProperty("p_user_id", userId);

        service.callRpc("lock_seat", body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Seat already taken or error.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface SeatCallback {
        void onSuccess(List<Seat> seats);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }
}
