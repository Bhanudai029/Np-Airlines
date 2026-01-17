package com.npairlines.data;

import com.npairlines.data.service.SupabaseService;
import com.npairlines.utils.Constants;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

public class SupabaseClient {
    private static SupabaseClient instance;
    private final SupabaseService service;
    private String accessToken = null;

    private SupabaseClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request.Builder builder = chain.request().newBuilder();
                    builder.addHeader("apikey", Constants.SUPABASE_KEY);
                    if (accessToken != null) {
                        builder.addHeader("Authorization", "Bearer " + accessToken);
                    } else {
                        builder.addHeader("Authorization", "Bearer " + Constants.SUPABASE_KEY);
                    }
                    return chain.proceed(builder.build());
                }
            })
            .build();

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Constants.SUPABASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        service = retrofit.create(SupabaseService.class);
    }

    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            instance = new SupabaseClient();
        }
        return instance;
    }

    public SupabaseService getService() {
        return service;
    }

    public void setAccessToken(String token) {
        this.accessToken = token;
    }
    
    public String getAccessToken() {
        return this.accessToken;
    }
}
