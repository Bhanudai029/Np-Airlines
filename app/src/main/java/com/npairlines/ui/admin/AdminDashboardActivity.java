package com.npairlines.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.npairlines.R;
import com.npairlines.data.model.Flight;
import com.npairlines.data.repository.FlightRepository;
import com.npairlines.ui.flight.FlightAdapter;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView rvAllFlights;
    private FlightRepository repository;
    private FlightAdapter adapter;
    private Button btnAddFlight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        rvAllFlights = findViewById(R.id.rv_all_flights);
        btnAddFlight = findViewById(R.id.btn_add_flight);
        
        repository = new FlightRepository();
        adapter = new FlightAdapter(); // Reusing FlightAdapter for simplicity
        
        rvAllFlights.setLayoutManager(new LinearLayoutManager(this));
        rvAllFlights.setAdapter(adapter);
        
        loadFlights();
        
        btnAddFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Simplified: Just show toast
                Toast.makeText(AdminDashboardActivity.this, "Add Flight Feature Placeholder", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFlights() {
        // Fetch all flights (no filter)
        repository.searchFlights("", "", new FlightRepository.FlightSearchCallback() {
            @Override
            public void onSuccess(List<Flight> flights) {
                adapter.setFlights(flights);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminDashboardActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
