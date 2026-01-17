package com.npairlines.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.npairlines.R;

public class BookingActivity extends AppCompatActivity {

    private EditText etName, etPassport, etPhone;
    private Button btnProceedPayment;
    private String flightId, seatId, seatNumber;
    private int passengerCount = 1;
    private double basePrice = 500.00;
    private double totalPrice = 500.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        flightId = getIntent().getStringExtra("FLIGHT_ID");
        seatId = getIntent().getStringExtra("SEAT_ID");
        seatNumber = getIntent().getStringExtra("SEAT_NUMBER");
        passengerCount = getIntent().getIntExtra("PASSENGER_COUNT", 1);
        
        // Calculate total price
        totalPrice = basePrice * passengerCount;

        etName = findViewById(R.id.et_fullname);
        etPassport = findViewById(R.id.et_passport);
        etPhone = findViewById(R.id.et_phone);
        btnProceedPayment = findViewById(R.id.btn_proceed_payment);
        
        TextView tvSummary = findViewById(R.id.tv_booking_summary);
        tvSummary.setText("Flight: NP101\nSeats: " + seatNumber + "\nPassengers: " + passengerCount + "\nTotal: NPR " + totalPrice);
        
        btnProceedPayment.setText("Pay NPR " + totalPrice);

        btnProceedPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    Intent intent = new Intent(BookingActivity.this, PaymentActivity.class);
                    intent.putExtra("FLIGHT_ID", flightId);
                    intent.putExtra("SEAT_ID", seatId);
                    intent.putExtra("SEAT_NUMBER", seatNumber); // Pass seat number too if needed
                    intent.putExtra("PASSENGER_NAME", etName.getText().toString());
                    intent.putExtra("AMOUNT", totalPrice);
                    intent.putExtra("PASSENGER_COUNT", passengerCount);
                    startActivity(intent);
                }
            }
        });
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etName.getText())) {
            etName.setError("Required");
            return false;
        }
        if (TextUtils.isEmpty(etPassport.getText())) {
            etPassport.setError("Required");
            return false;
        }
        return true;
    }
}
