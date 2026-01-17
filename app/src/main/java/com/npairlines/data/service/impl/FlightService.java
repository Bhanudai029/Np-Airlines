package com.npairlines.data.service.impl;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.npairlines.data.SupabaseClient;
import com.npairlines.data.model.Flight;
import com.npairlines.data.service.SupabaseService;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlightService {
    private static final String TAG = "FlightService";
    private final SupabaseService service;
    private static FlightService instance;

    private FlightService() {
        this.service = SupabaseClient.getInstance().getService();
    }

    public static synchronized FlightService getInstance() {
        if (instance == null) {
            instance = new FlightService();
        }
        return instance;
    }

    public interface FlightCallback {
        void onSuccess(List<Flight> flights);
        void onError(String message);
    }

    public void searchFlights(String origin, String destination, final FlightCallback callback) {
        Map<String, String> filters = new HashMap<>();
        filters.put("status", "eq.SCHEDULED");
        if (origin != null && !origin.isEmpty()) {
            filters.put("origin_code", "eq." + origin);
        }
        if (destination != null && !destination.isEmpty()) {
            filters.put("destination_code", "eq." + destination);
        }
        
        Log.d(TAG, "Searching flights: origin=" + origin + ", dest=" + destination);
        Log.d(TAG, "Filters: " + filters.toString());

        service.getTable("flights", filters, "0-100").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        Log.d(TAG, "Response JSON: " + json);
                        List<Flight> flights = new Gson().fromJson(json, new TypeToken<List<Flight>>(){}.getType());
                        Log.d(TAG, "Parsed " + (flights != null ? flights.size() : 0) + " flights");
                        callback.onSuccess(flights);
                    } catch (Exception e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                        callback.onError("Parse error: " + e.getMessage());
                    }
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Log.e(TAG, "Failed: code=" + response.code() + ", error=" + errorBody);
                    callback.onError("Failed (code " + response.code() + "): " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Network failure: " + t.getMessage());
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Client-side filtering/sorting for demo (Server-side preferred but requires complex query building)
    public void sortFlights(List<Flight> flights, final boolean priceAscending) {
        Collections.sort(flights, new Comparator<Flight>() {
            @Override
            public int compare(Flight f1, Flight f2) {
                if (priceAscending) return Double.compare(f1.getPriceEconomy(), f2.getPriceEconomy());
                else return Double.compare(f2.getPriceEconomy(), f1.getPriceEconomy());
            }
        });
    }
}
