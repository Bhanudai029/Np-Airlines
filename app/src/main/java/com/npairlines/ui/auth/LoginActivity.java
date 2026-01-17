package com.npairlines.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.npairlines.MainActivity;
import com.npairlines.R;
import com.npairlines.data.SupabaseClient;
import com.npairlines.data.repository.AuthRepository;
import com.npairlines.data.service.impl.AuthService;
import com.google.gson.JsonObject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoToRegister;
    private AuthRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repository = new AuthRepository();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoToRegister = findViewById(R.id.btn_register);
        
        btnGoToRegister = findViewById(R.id.btn_register);
        
        btnLogin.setOnClickListener(v -> login());
        
        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        checkIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntent(intent);
    }

    private void checkIntent(Intent intent) {
        if (intent != null && intent.getData() != null) {
            android.net.Uri uri = intent.getData();
            if ("npairlines".equals(uri.getScheme()) && "auth".equals(uri.getHost()) && "/callback".equals(uri.getPath())) {
                // Supabase returns tokens in fragment part usually: #access_token=...&refresh_token=...
                String fragment = uri.getFragment();
                if (fragment != null && fragment.contains("access_token=")) {
                    handleDeepLinkToken(fragment);
                }
            }
        }
    }

    private void handleDeepLinkToken(String fragment) {
        String accessToken = null;
        String refreshToken = null;
        
        String[] parts = fragment.split("&");
        for (String part : parts) {
            if (part.startsWith("access_token=")) {
                accessToken = part.substring("access_token=".length());
            } else if (part.startsWith("refresh_token=")) {
                refreshToken = part.substring("refresh_token=".length());
            }
        }

        if (accessToken != null) {
            SupabaseClient.getInstance().setAccessToken(accessToken);
            
            // Save token
            com.npairlines.utils.SessionManager sessionManager = new com.npairlines.utils.SessionManager(this);
            sessionManager.saveToken(accessToken);

            Toast.makeText(this, "Email Verified! Checking profile...", Toast.LENGTH_SHORT).show();
            
            // Check if user has completed onboarding before redirecting
            checkUserProfileAndNavigate(accessToken);
        }
    }

    private void login() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        repository.signIn(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String token) {
                // Save token
                com.npairlines.utils.SessionManager sessionManager = new com.npairlines.utils.SessionManager(LoginActivity.this);
                sessionManager.saveToken(token);

                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                
                // Check if user has completed onboarding
                checkUserProfileAndNavigate(token);
            }

            @Override
            public void onError(String message) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void checkUserProfileAndNavigate(String token) {
        // First get the user ID
        AuthService.getInstance().getUser(token, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(JsonObject userData) {
                String userId = userData.get("id").getAsString();
                
                // Query users table to check if profile exists
                SupabaseClient.getInstance().getService()
                    .getUserProfile("eq." + userId, "full_name")
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            btnLogin.setEnabled(true);
                            
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String json = response.body().string();
                                    // If response is empty array [] or full_name is null, user needs onboarding
                                    if (json.equals("[]") || json.contains("\"full_name\":null")) {
                                        navigateToOnboarding();
                                    } else {
                                        // User has profile, go to main
                                        navigateToMain();
                                    }
                                } catch (Exception e) {
                                    // On error, default to onboarding
                                    navigateToOnboarding();
                                }
                            } else {
                                // No profile found, needs onboarding
                                navigateToOnboarding();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            btnLogin.setEnabled(true);
                            // On network error, try onboarding (safer option)
                            navigateToOnboarding();
                        }
                    });
            }

            @Override
            public void onError(String message) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Error getting user: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void navigateToOnboarding() {
        Intent intent = new Intent(LoginActivity.this, OnboardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
