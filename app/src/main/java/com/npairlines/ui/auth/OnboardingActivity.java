package com.npairlines.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.npairlines.MainActivity;
import com.npairlines.R;
import com.npairlines.data.SupabaseClient;
import com.google.gson.JsonObject;
import com.npairlines.data.service.impl.AuthService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnboardingActivity extends AppCompatActivity {

    private com.google.android.material.textfield.TextInputLayout tilFullName, tilDob;
    private EditText etFullName, etDob;
    private CheckBox cbTerms;
    private android.widget.TextView tvTermsLink;
    private Button btnAllSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        tilFullName = findViewById(R.id.til_fullname);
        tilDob = findViewById(R.id.til_dob);

        etFullName = findViewById(R.id.et_fullname);
        etDob = findViewById(R.id.et_dob);
        cbTerms = findViewById(R.id.cb_terms);
        btnAllSet = findViewById(R.id.btn_all_set);
        tvTermsLink = findViewById(R.id.tv_terms_link); // Initialized tvTermsLink

        etDob.setFocusable(false);
        etDob.setClickable(true);
        etDob.setOnClickListener(v -> showDatePicker());

        // Live Validation for Name
        etFullName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateName();
            }
        });

        tvTermsLink.setOnClickListener(v -> {
            startActivity(new Intent(OnboardingActivity.this, TermsActivity.class));
        });

        btnAllSet.setOnClickListener(v -> {
            if (validateName() && validateDob() && validateTerms()) {
                 submitProfile();
            }
        });
    }

    private boolean validateName() {
        String fullName = etFullName.getText().toString().trim();
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Name is required");
            return false;
        }
        if (fullName.length() > 25) {
            tilFullName.setError("Name must be 25 characters or less");
            return false;
        }
        if (!fullName.matches("^[a-zA-Z\\s]+$")) {
            tilFullName.setError("Name can only contain letters and spaces");
            return false;
        }
        if (isRepetitive(fullName)) {
            tilFullName.setError("Invalid name");
            return false;
        }
        tilFullName.setError(null);
        return true;
    }

    private boolean validateDob() {
         String dobStr = etDob.getText().toString();
         if (TextUtils.isEmpty(dobStr)) {
             tilDob.setError("Date of Birth is required");
             return false;
         }
         
         // Calculate Age
         try {
             java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
             java.util.Date date = sdf.parse(dobStr);
             java.util.Calendar dob = java.util.Calendar.getInstance();
             dob.setTime(date);
             
             java.util.Calendar today = java.util.Calendar.getInstance();
             
             int age = today.get(java.util.Calendar.YEAR) - dob.get(java.util.Calendar.YEAR);
             if (today.get(java.util.Calendar.DAY_OF_YEAR) < dob.get(java.util.Calendar.DAY_OF_YEAR)) {
                 age--;
             }
             
             if (age < 16) {
                 tilDob.setError("You must be at least 16 years old");
                 return false;
             }
             
         } catch (java.text.ParseException e) {
             tilDob.setError("Invalid Date Format");
             return false;
         }

         tilDob.setError(null);
         return true;
    }

    private boolean validateTerms() {
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the Terms and Conditions", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showDatePicker() {
        final java.util.Calendar c = java.util.Calendar.getInstance();
        int year = c.get(java.util.Calendar.YEAR);
        int month = c.get(java.util.Calendar.MONTH);
        int day = c.get(java.util.Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(java.util.Locale.US, "%d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    etDob.setText(selectedDate);
                    tilDob.setError(null); // Clear error on selection
                }, year, month, day);
        
        // Restrict to reasonable age (e.g., max today, min 1900)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        
        datePickerDialog.show();
    }

    private void submitProfile() {
        String fullName = etFullName.getText().toString().trim();
        String dob = etDob.getText().toString().trim();

        // Get current user to find UUID
        String token = SupabaseClient.getInstance().getAccessToken();
        if (token == null) {
             Toast.makeText(this, "Session invalid, please login again", Toast.LENGTH_SHORT).show();
             startActivity(new Intent(this, LoginActivity.class));
             finish();
             return;
        }

        // 1. Fetch User ID
        AuthService.getInstance().getUser(token, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(JsonObject userData) {
                // userData contains "id"
                String userId = userData.get("id").getAsString();
                saveUserToDb(userId, fullName, dob);
            }

            @Override
            public void onError(String message) {
                // If fetching user fails, we can't save safely.
                Toast.makeText(OnboardingActivity.this, "Failed to get identity: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToDb(String userId, String name, String dob) {
        JsonObject userRow = new JsonObject();
        userRow.addProperty("id", userId);
        userRow.addProperty("full_name", name);
        userRow.addProperty("date_of_birth", dob); // Ensure column matches DB
        // Phone number removed

        SupabaseClient.getInstance().getService().insert("users", userRow, "resolution=merge-duplicates").enqueue(new Callback<Void>() {
             @Override
             public void onResponse(Call<Void> call, Response<Void> response) {
                 if (response.isSuccessful()) {
                      Toast.makeText(OnboardingActivity.this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();
                      Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
                      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                      startActivity(intent);
                      finish();
                 } else {
                      try { String e = response.errorBody() != null ? response.errorBody().string() : "?"; Toast.makeText(OnboardingActivity.this, "Save failed (" + response.code() + "): " + e, Toast.LENGTH_LONG).show(); } catch (Exception ex) { Toast.makeText(OnboardingActivity.this, "Save failed: " + response.message(), Toast.LENGTH_SHORT).show(); }
                 }
             }

             @Override
             public void onFailure(Call<Void> call, Throwable t) {
                 Toast.makeText(OnboardingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
             }
        });
    }
    private boolean isRepetitive(String s) {
        if (s == null || s.length() <= 2) return false;
        char first = Character.toLowerCase(s.charAt(0));
        for (int i = 1; i < s.length(); i++) {
            if (Character.toLowerCase(s.charAt(i)) != first) return false;
        }
        return true;
    }
}
