package com.npairlines.data.service.impl;

import android.util.Log;
import com.npairlines.utils.Constants;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.json.JSONObject;

public class RealtimeService {
    private static RealtimeService instance;
    private WebSocket webSocket;
    private SeatUpdateListener listener;
    private final OkHttpClient client;

    // Supabase Realtime WSS URL
    private static final String WSS_URL = "wss://czyzknaikvxtyexrkzpi.supabase.co/realtime/v1/websocket?apikey=" + Constants.SUPABASE_KEY + "&vsn=1.0.0";

    private RealtimeService() {
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    public static synchronized RealtimeService getInstance() {
        if (instance == null) {
            instance = new RealtimeService();
        }
        return instance;
    }

    public void setListener(SeatUpdateListener listener) {
        this.listener = listener;
    }

    public void connect() {
        Request request = new Request.Builder()
                .url(WSS_URL)
                .build();
        
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("Realtime", "Connected");
                joinChannel();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("Realtime", "Msg: " + text);
                handleMessage(text);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("Realtime", "Error: " + t.getMessage());
            }
        });
    }

    private void joinChannel() {
        // Phoenix Channel Join payload
        String joinPayload = "{\"topic\":\"realtime:public:seats\",\"event\":\"phx_join\",\"payload\":{},\"ref\":\"1\"}";
        webSocket.send(joinPayload);
    }
    
    private void handleMessage(String text) {
        try {
            JSONObject json = new JSONObject(text);
            // Basic parsing for Postgres Changes
            // Expected format: { "event": "INSERT" or "UPDATE", "payload": { "data": { ... } } }
            // Supabase Realtime format is slightly nested.
            // Simplified check:
            if (listener != null) {
                // Determine if it's a seat update and notify
                // This is a rough parsing for the sake of the demo
                if (text.contains("seats") && (text.contains("INSERT") || text.contains("UPDATE"))) {
                     // In a real app, parse fully. Here we just trigger a refresh.
                     listener.onSeatUpdated();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing");
        }
    }

    public interface SeatUpdateListener {
        void onSeatUpdated();
    }
}
