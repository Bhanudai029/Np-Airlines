package com.npairlines.data.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.npairlines.data.SupabaseClient;
import com.npairlines.data.model.Flight;
import com.npairlines.data.service.SupabaseService;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlightRepository {
    private final SupabaseService service;
    private final Gson gson;

    public FlightRepository() {
        this.service = SupabaseClient.getInstance().getService();
        this.gson = new Gson();
    }

    public void searchFlights(String origin, String destination, final FlightSearchCallback callback) {
        Map<String, String> filters = new HashMap<>();
        if (origin != null && !origin.isEmpty()) {
            filters.put("origin_code", "eq." + origin); // PostgREST syntax
        }
        if (destination != null && !destination.isEmpty()) {
            filters.put("destination_code", "eq." + destination);
        }
        // Ensure we only show scheduled flights
        filters.put("status", "eq.SCHEDULED");

        service.getTable("flights", filters, "0-100").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        Type listType = new TypeToken<List<Flight>>(){}.getType();
                        List<Flight> flights = gson.fromJson(json, listType);
                        callback.onSuccess(flights);
                    } catch (IOException e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                } else {
                    callback.onError("Search failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface FlightSearchCallback {
        void onSuccess(List<Flight> flights);
        void onError(String message);
    }
}
