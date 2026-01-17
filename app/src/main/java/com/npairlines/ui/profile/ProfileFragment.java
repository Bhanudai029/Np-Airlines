package com.npairlines.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.npairlines.R;
import com.npairlines.ui.auth.LoginActivity;

public class ProfileFragment extends Fragment {

    private LinearLayout loadingContainer;
    private LinearLayout profileContainer;
    private android.widget.TextView tvUsername;
    private android.widget.ImageView ivProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        loadingContainer = view.findViewById(R.id.loading_container);
        profileContainer = view.findViewById(R.id.profile_container);
        tvUsername = view.findViewById(R.id.tv_username);
        ivProfile = view.findViewById(R.id.iv_profile);
        
        // Show loading, hide content
        showLoading(true);
        
        // Fetch profile data
        fetchUserProfile();

        Button btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            try {
                // Clear Local Session
                com.npairlines.utils.SessionManager sessionManager = new com.npairlines.utils.SessionManager(getContext());
                sessionManager.clear();

                if (com.npairlines.data.service.impl.AuthService.getInstance() != null) {
                    com.npairlines.data.service.impl.AuthService.getInstance().logout();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
    
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingContainer.setVisibility(View.VISIBLE);
            profileContainer.setVisibility(View.GONE);
        } else {
            loadingContainer.setVisibility(View.GONE);
            profileContainer.setVisibility(View.VISIBLE);
        }
    }
    
    private void fetchUserProfile() {
        String token = com.npairlines.data.SupabaseClient.getInstance().getAccessToken();
        if (token == null) {
            showLoading(false);
            tvUsername.setText("Unknown");
            return;
        }
        
        com.npairlines.data.service.impl.AuthService.getInstance().getUser(token, new com.npairlines.data.service.impl.AuthService.AuthCallback() {
            @Override
            public void onSuccess(com.google.gson.JsonObject userData) {
                 String userId = userData.get("id").getAsString();
                 fetchUserDetailsFromDb(userId);
            }
            @Override
            public void onError(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        tvUsername.setText("Unknown");
                    });
                }
            }
        });
    }

    private void fetchUserDetailsFromDb(String userId) {
        java.util.Map<String, String> filters = new java.util.HashMap<>();
        filters.put("id", "eq." + userId);
        
        com.npairlines.data.SupabaseClient.getInstance().getService().getTable("users", filters, "0-1")
            .enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                     if (response.isSuccessful() && response.body() != null) {
                         try {
                             String json = response.body().string();
                             java.util.List<com.google.gson.JsonObject> list = new com.google.gson.Gson().fromJson(json, new com.google.gson.reflect.TypeToken<java.util.List<com.google.gson.JsonObject>>(){}.getType());
                             if (!list.isEmpty()) {
                                 String name = list.get(0).get("full_name").getAsString();
                                 loadAvatarThenShow(name);
                             } else {
                                 if (getActivity() != null) {
                                     getActivity().runOnUiThread(() -> {
                                         showLoading(false);
                                         tvUsername.setText("Unknown");
                                     });
                                 }
                             }
                         } catch (Exception e) {
                             e.printStackTrace();
                             if (getActivity() != null) {
                                 getActivity().runOnUiThread(() -> {
                                     showLoading(false);
                                     tvUsername.setText("Unknown");
                                 });
                             }
                         }
                     } else {
                         if (getActivity() != null) {
                             getActivity().runOnUiThread(() -> {
                                 showLoading(false);
                                 tvUsername.setText("Unknown");
                             });
                         }
                     }
                }
                @Override
                public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            tvUsername.setText("Unknown");
                        });
                    }
                }
            });
    }
    
    private void loadAvatarThenShow(String name) {
        // Use only first letter for cleaner look
        String initial = name != null && !name.isEmpty() ? String.valueOf(name.charAt(0)).toUpperCase() : "?";
        String url = "https://ui-avatars.com/api/?name=" + initial + "&background=0D8ABC&color=fff&size=200&rounded=true&bold=true&font-size=0.5";
        
        new Thread(() -> {
            android.graphics.Bitmap bmp = null;
            try {
                java.io.InputStream in = new java.net.URL(url).openStream();
                bmp = android.graphics.BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            final android.graphics.Bitmap finalBmp = bmp;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Set the name
                    tvUsername.setText(name);
                    
                    // Set avatar if loaded
                    if (finalBmp != null) {
                        ivProfile.setImageBitmap(finalBmp);
                    } else {
                        // Fallback: set a colored background with no image
                        ivProfile.setBackgroundColor(0xFF0D8ABC);
                    }
                    
                    // NOW show the profile content
                    showLoading(false);
                });
            }
        }).start();
    }
}
