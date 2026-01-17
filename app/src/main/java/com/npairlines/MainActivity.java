package com.npairlines;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.npairlines.data.SupabaseClient;
import com.npairlines.data.service.impl.AuthService;
import com.npairlines.ui.auth.LoginActivity;
import com.npairlines.ui.auth.OnboardingActivity;
import com.npairlines.ui.flight.HomeFragment;
import com.npairlines.ui.profile.ProfileFragment;
import com.npairlines.ui.booking.MyBookingsFragment;
import com.google.gson.JsonObject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for session
        com.npairlines.utils.SessionManager sessionManager = new com.npairlines.utils.SessionManager(this);
        String token = sessionManager.getToken();

        if (token != null) {
            SupabaseClient.getInstance().setAccessToken(token);
            
            // Verify user has completed onboarding
            verifyOnboardingComplete(token, savedInstanceState);
        } else {
            // No user logged in, redirect to Login
            redirectToLogin();
        }
    }
    
    private void verifyOnboardingComplete(String token, Bundle savedInstanceState) {
        AuthService.getInstance().getUser(token, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(JsonObject userData) {
                String userId = userData.get("id").getAsString();
                
                // Check if user profile exists in database
                SupabaseClient.getInstance().getService()
                    .getUserProfile("eq." + userId, "full_name")
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String json = response.body().string();
                                    if (json.equals("[]") || json.contains("\"full_name\":null")) {
                                        // No profile, redirect to onboarding
                                        redirectToOnboarding();
                                    } else {
                                        // Profile exists, continue to main app
                                        setupMainActivity(savedInstanceState);
                                    }
                                } catch (Exception e) {
                                    // On error, setup main (already logged in)
                                    setupMainActivity(savedInstanceState);
                                }
                            } else {
                                // Profile not found, redirect to onboarding
                                redirectToOnboarding();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            // Network error, try to continue (user is logged in)
                            setupMainActivity(savedInstanceState);
                        }
                    });
            }

            @Override
            public void onError(String message) {
                // Token might be invalid, redirect to login
                redirectToLogin();
            }
        });
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void redirectToOnboarding() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void setupMainActivity(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                
                if (itemId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.navigation_bookings) {
                    selectedFragment = new MyBookingsFragment();
                } else if (itemId == R.id.navigation_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();
                }
                return true;
            }
        });

        // Default screen
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new HomeFragment())
                .commit();
        }
    }

    @Override
    public void onBackPressed() {
        // Inflate custom view
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);
        
        android.widget.TextView tvTitle = dialogView.findViewById(R.id.dialog_title);
        tvTitle.setText("Exit Np Airlines?");
        
        android.widget.TextView tvMessage = dialogView.findViewById(R.id.dialog_message);
        tvMessage.setText("Are you sure you want to quit the application?");

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setView(dialogView) // Use Custom View
            .setPositiveButton("Yes, Exit", (dialog, which) -> {
                finishAffinity();
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            })
            .setCancelable(true)
            .show();
    }
}
