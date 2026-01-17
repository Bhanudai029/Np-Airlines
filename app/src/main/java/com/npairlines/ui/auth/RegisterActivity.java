package com.npairlines.ui.auth;

import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.npairlines.R;
import com.npairlines.data.service.impl.AuthService;
import com.google.gson.JsonObject;

public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Reusing login layout for simplicity

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_login); // Reusing button ID
        btnRegister.setText("Register");

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                 AuthService.getInstance().register(email, password, new AuthService.AuthCallback() {
                     @Override
                     public void onSuccess(JsonObject data) {
                         // Ensure UI updates run on the main thread
                         runOnUiThread(() -> {
                             // Inflate custom view
                             android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);
                             
                             android.widget.TextView tvTitle = dialogView.findViewById(R.id.dialog_title);
                             tvTitle.setText("Verification Link Sent");
                             
                             android.widget.TextView tvMessage = dialogView.findViewById(R.id.dialog_message);
                             tvMessage.setText("A verification link has been sent to " + email + ". Please check your email to verify your account.");

                             new com.google.android.material.dialog.MaterialAlertDialogBuilder(RegisterActivity.this)
                                 .setView(dialogView) // Use Custom View
                                 .setPositiveButton("OK", (dialog, which) -> {
                                     // Return to Login screen
                                     finish(); 
                                 })
                                 .setCancelable(false)
                                 .show();
                         });
                     }
                     
                     @Override
                     public void onError(String message) {
                         Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                     }
                 });
            }
        });
        
        // Hide the "New to Np Airlines?" prompt
        if (findViewById(R.id.ll_signup_prompt) != null) {
            findViewById(R.id.ll_signup_prompt).setVisibility(android.view.View.GONE);
        }
    }
}
