package com.npairlines.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.npairlines.MainActivity;
import com.npairlines.R;
import com.npairlines.data.service.impl.AuthService;
import com.google.gson.JsonObject;

public class OtpActivity extends AppCompatActivity {
    private EditText etOtp;
    private Button btnVerify;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        email = getIntent().getStringExtra("EMAIL");
        etOtp = findViewById(R.id.et_otp);
        btnVerify = findViewById(R.id.btn_verify);

        btnVerify.setOnClickListener(v -> {
            String otp = etOtp.getText().toString();
            if(!TextUtils.isEmpty(otp)) {
                verify(otp);
            }
        });
    }
    
    private void verify(String otp) {
        AuthService.getInstance().verifyOtp(email, otp, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(JsonObject data) {
                Toast.makeText(OtpActivity.this, "Verified!", Toast.LENGTH_SHORT).show();
                 Intent intent = new Intent(OtpActivity.this, OnboardingActivity.class);
                 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                 startActivity(intent);
                 finish();
            }
            
            @Override
            public void onError(String message) {
                Toast.makeText(OtpActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
