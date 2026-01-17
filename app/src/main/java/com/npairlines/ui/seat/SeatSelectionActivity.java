package com.npairlines.ui.seat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.npairlines.R;
import com.npairlines.data.model.Seat;
import com.npairlines.data.repository.SeatRepository;
import com.npairlines.data.service.impl.BookingService;
import com.npairlines.data.service.impl.RealtimeService;
import com.npairlines.ui.booking.BookingActivity;
import java.util.List;

public class SeatSelectionActivity extends AppCompatActivity implements RealtimeService.SeatUpdateListener {

    private int passengerCount = 1;
    private java.util.Set<Seat> selectedSeats = new java.util.HashSet<>();
    
    private SeatMapView seatMapView;
    private Button btnConfirmSeat;
    private SeatRepository repository;
    private String flightId;
    private LinearLayout selectedInfoContainer;
    private TextView tvSelectedSeat, tvSelectedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        flightId = getIntent().getStringExtra("FLIGHT_ID");
        passengerCount = getIntent().getIntExtra("PASSENGER_COUNT", 1);
        
        seatMapView = findViewById(R.id.seat_map_view);
        btnConfirmSeat = findViewById(R.id.btn_continue);
        selectedInfoContainer = findViewById(R.id.selected_info_container);
        tvSelectedSeat = findViewById(R.id.tv_selected_seat);
        tvSelectedClass = findViewById(R.id.tv_selected_class);
        repository = new SeatRepository();
        
        // Configure MapView
        seatMapView.setMaxSeats(passengerCount);
        
        // Update title with count
        TextView tvTitle = findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setText("Select " + passengerCount + " Seat" + (passengerCount > 1 ? "s" : ""));
        }

        loadSeats();
        
        // Connect Realtime
        RealtimeService.getInstance().connect();
        RealtimeService.getInstance().setListener(this);

        seatMapView.setListener(new SeatMapView.OnSeatClickListener() {
            @Override
            public void onSeatClick(Seat seat, int totalSelected) {
                // Update local tracking
                List<Seat> currentSelection = seatMapView.getSelectedSeats();
                selectedSeats.clear();
                selectedSeats.addAll(currentSelection);
                
                if (!selectedSeats.isEmpty()) {
                    selectedInfoContainer.setVisibility(android.view.View.VISIBLE);
                    
                    // Build comma separated string
                    StringBuilder sb = new StringBuilder();
                    String seatClass = "";
                    for (Seat s : selectedSeats) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(s.getSeatNumber());
                        seatClass = s.getSeatClass(); // Just take the last one for now
                    }
                    
                    tvSelectedSeat.setText(sb.toString());
                    tvSelectedClass.setText((seatClass != null ? seatClass : "ECONOMY") + " (" + totalSelected + "/" + passengerCount + ")");
                } else {
                    // Hide if nothing selected
                    selectedInfoContainer.setVisibility(android.view.View.GONE);
                }
                
                // Enable if count matches
                if (totalSelected == passengerCount) {
                    btnConfirmSeat.setEnabled(true);
                    btnConfirmSeat.setText("Confirm Selection (" + totalSelected + ")");
                } else {
                    btnConfirmSeat.setEnabled(false);
                    if (totalSelected == 0) {
                        btnConfirmSeat.setText("Select " + passengerCount + " Seat" + (passengerCount > 1 ? "s" : ""));
                    } else {
                        btnConfirmSeat.setText("Select " + (passengerCount - totalSelected) + " more");
                    }
                }
            }
        });

        btnConfirmSeat.setOnClickListener(v -> lockSeatAndProceed());
    }

    private void loadSeats() {
        repository.getSeats(flightId, new SeatRepository.SeatCallback() {
            @Override
            public void onSuccess(List<Seat> seats) {
                seatMapView.setSeats(seats);
            }
            @Override
            public void onError(String message) {
                Toast.makeText(SeatSelectionActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void lockSeatAndProceed() {
        if (selectedSeats.isEmpty() || selectedSeats.size() != passengerCount) return;
        
        // Get real user ID from auth service
        String token = com.npairlines.data.SupabaseClient.getInstance().getAccessToken();
        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnConfirmSeat.setEnabled(false);
        Toast.makeText(this, "Locking seats...", Toast.LENGTH_SHORT).show();
        
        com.npairlines.data.service.impl.AuthService.getInstance().getUser(token, new com.npairlines.data.service.impl.AuthService.AuthCallback() {
            @Override
            public void onSuccess(com.google.gson.JsonObject userData) {
                String userId = userData.get("id").getAsString();
                lockSeatsRecursive(new java.util.ArrayList<>(selectedSeats), 0, userId);
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnConfirmSeat.setEnabled(true);
                    Toast.makeText(SeatSelectionActivity.this, "Auth error: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    // Recursive function to lock all seats
    private void lockSeatsRecursive(final List<Seat> seatsToLock, final int index, final String userId) {
        if (index >= seatsToLock.size()) {
            // All locked successfully
            proceedToBooking(userId);
            return;
        }
        
        Seat seat = seatsToLock.get(index);
        BookingService.getInstance().lockSeat(flightId, seat.getSeatNumber(), userId, new BookingService.SimpleCallback() {
            @Override
            public void onSuccess() {
                // Lock next one
                lockSeatsRecursive(seatsToLock, index + 1, userId);
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnConfirmSeat.setEnabled(true);
                    Toast.makeText(SeatSelectionActivity.this, "Failed to lock " + seat.getSeatNumber() + ": " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void proceedToBooking(String userId) {
        StringBuilder seatIds = new StringBuilder();
        StringBuilder seatNumbers = new StringBuilder();
        
        for (Seat s : selectedSeats) {
            if (seatIds.length() > 0) seatIds.append(",");
            seatIds.append(s.getId());
            
            if (seatNumbers.length() > 0) seatNumbers.append(",");
            seatNumbers.append(s.getSeatNumber());
        }
    
        Intent intent = new Intent(SeatSelectionActivity.this, BookingActivity.class);
        intent.putExtra("FLIGHT_ID", flightId);
        intent.putExtra("SEAT_ID", seatIds.toString()); // Passing comma separated
        intent.putExtra("SEAT_NUMBER", seatNumbers.toString());
        intent.putExtra("USER_ID", userId);
        intent.putExtra("PASSENGER_COUNT", passengerCount);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSeatUpdated() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Determine if optimization needed (only fetch diff), for now reload all
                loadSeats();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RealtimeService.getInstance().disconnect();
    }
}
