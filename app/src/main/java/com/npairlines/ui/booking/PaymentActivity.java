package com.npairlines.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.npairlines.MainActivity;
import com.npairlines.R;
import com.npairlines.data.repository.BookingRepository;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvAmount;
    private Button btnPay;
    private BookingRepository repository;
    private String flightId, seatId, passengerName;
    private double amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        repository = new BookingRepository();
        
        flightId = getIntent().getStringExtra("FLIGHT_ID");
        seatId = getIntent().getStringExtra("SEAT_ID");
        passengerName = getIntent().getStringExtra("PASSENGER_NAME");
        amount = getIntent().getDoubleExtra("AMOUNT", 0.0);

        tvAmount = findViewById(R.id.tv_amount);
        btnPay = findViewById(R.id.btn_pay);
        
        tvAmount.setText("Total Amount: NPR " + amount);

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPayment();
            }
        });
    }

    private void processPayment() {
        // Get real user ID from auth service
        String token = com.npairlines.data.SupabaseClient.getInstance().getAccessToken();
        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnPay.setEnabled(false);
        btnPay.setText("Processing...");
        
        com.npairlines.data.service.impl.AuthService.getInstance().getUser(token, new com.npairlines.data.service.impl.AuthService.AuthCallback() {
            @Override
            public void onSuccess(com.google.gson.JsonObject userData) {
                String userId = userData.get("id").getAsString();
                
                repository.createBooking(userId, flightId, seatId, amount, new BookingRepository.BookingCallback() {
                    @Override
                    public void onSuccess(String bookingReference) {
                        Intent intent = new Intent(PaymentActivity.this, ConfirmationActivity.class);
                        intent.putExtra("REFERENCE", bookingReference);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(PaymentActivity.this, "Payment Failed: " + message, Toast.LENGTH_LONG).show();
                            btnPay.setEnabled(true);
                            btnPay.setText("Pay Now");
                        });
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(PaymentActivity.this, "Auth error: " + message, Toast.LENGTH_LONG).show();
                    btnPay.setEnabled(true);
                    btnPay.setText("Pay Now");
                });
            }
        });
    }
}
