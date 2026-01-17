package com.npairlines.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.npairlines.R;
import com.npairlines.data.service.impl.AuthService;
import com.google.gson.JsonObject;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Reuse login layout for simplicity, or create new.
        
        // Customizing UI for Reset Password
        etEmail = findViewById(R.id.et_email);
        findViewById(R.id.et_password).setVisibility(android.view.View.GONE);
        btnReset = findViewById(R.id.btn_login);
        btnReset.setText("Send Reset Link");
        
        if (findViewById(R.id.btn_register) != null) {
             findViewById(R.id.btn_register).setVisibility(android.view.View.GONE);
        }

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            if (!TextUtils.isEmpty(email)) {
                AuthService.getInstance().resetPassword(email, new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess(JsonObject data) {
                        Toast.makeText(ForgotPasswordActivity.this, "Reset link sent if email exists.", Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ForgotPasswordActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                etEmail.setError("Required");
            }
        });
    }
}
