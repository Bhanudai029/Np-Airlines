package com.npairlines.data.repository;

import com.google.gson.JsonObject;
import com.npairlines.data.SupabaseClient;
import com.npairlines.data.service.SupabaseService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private final SupabaseService service;

    public AuthRepository() {
        this.service = SupabaseClient.getInstance().getService();
    }

    public void signIn(String email, String password, final AuthCallback callback) {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        service.signIn(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Extract access_token
                    JsonObject data = response.body();
                    if (data.has("access_token")) {
                        String token = data.get("access_token").getAsString();
                        SupabaseClient.getInstance().setAccessToken(token);
                        callback.onSuccess(token);
                    } else {
                        callback.onError("No access token found");
                    }
                } else {
                    callback.onError("Login failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface AuthCallback {
        void onSuccess(String token);
        void onError(String message);
    }
}
