package com.npairlines.data.service;

import com.google.gson.JsonObject;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.PATCH;

public interface SupabaseService {
    // Auth
    @POST("/auth/v1/signup")
    Call<JsonObject> signUp(@Body JsonObject body);

    @POST("/auth/v1/token?grant_type=password")
    Call<JsonObject> signIn(@Body JsonObject body);
    
    @POST("/auth/v1/verify")
    Call<JsonObject> verifyOtp(@Body JsonObject body);
    
    @POST("/auth/v1/logout")
    Call<Void> signOut();

    @GET("/auth/v1/user")
    Call<JsonObject> getUser();

    // Database (Generic)
    @GET("/rest/v1/{table}")
    Call<ResponseBody> getTable(
        @Path("table") String table,
        @QueryMap Map<String, String> filters,
        @Header("Range") String range
    );
    
    @POST("/rest/v1/{table}")
    Call<Void> insert(@Path("table") String table, @Body JsonObject body, @Header("Prefer") String prefer);
    
    @PATCH("/rest/v1/{table}")
    Call<Void> update(@Path("table") String table, @QueryMap Map<String, String> filters, @Body JsonObject body);
    
    @POST("/rest/v1/rpc/{function}")
    Call<JsonObject> callRpc(@Path("function") String function, @Body JsonObject body);
    
    // Check if user profile exists
    @GET("/rest/v1/users")
    Call<ResponseBody> getUserProfile(@Query("id") String idFilter, @Query("select") String select);
}
