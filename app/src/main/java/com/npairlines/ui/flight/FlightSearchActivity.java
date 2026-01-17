package com.npairlines.ui.flight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.npairlines.R;
import com.npairlines.data.model.Flight;
import com.npairlines.ui.auth.LoginActivity;
import com.npairlines.ui.base.BaseActivity;
import com.npairlines.ui.seat.SeatSelectionActivity;

public class FlightSearchActivity extends BaseActivity<FlightSearchViewModel> implements FlightAdapter.OnFlightClickListener {

    private EditText etOrigin, etDestination;
    private Button btnSearch;
    private RecyclerView rvFlights;
    private FlightAdapter adapter;
    private FloatingActionButton fabLogin;

    @Override
    protected FlightSearchViewModel getViewModel() {
        return new ViewModelProvider(this).get(FlightSearchViewModel.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_search);
        
        // BaseActivity handles viewModel creation via getViewModel() and observers

        etOrigin = findViewById(R.id.et_origin);
        etDestination = findViewById(R.id.et_destination);
        btnSearch = findViewById(R.id.btn_search);
        rvFlights = findViewById(R.id.rv_flights);
        fabLogin = findViewById(R.id.fab_login);

        rvFlights.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FlightAdapter();
        adapter.setOnFlightClickListener(this);
        rvFlights.setAdapter(adapter);

        // Observe specific data (flights)
        // Loading and Error are handled by BaseActivity
        viewModel.getFlights().observe(this, flights -> {
            adapter.setFlights(flights);
        });

        btnSearch.setOnClickListener(v -> {
            String origin = etOrigin.getText().toString();
            String destination = etDestination.getText().toString();
            viewModel.search(origin, destination, null);
        });
        
        fabLogin.setOnClickListener(v -> {
            startActivity(new Intent(FlightSearchActivity.this, LoginActivity.class));
        });
    }

    @Override
    public void onFlightClick(Flight flight) {
        Intent intent = new Intent(this, SeatSelectionActivity.class);
        intent.putExtra("FLIGHT_ID", flight.getId());
        startActivity(intent);
    }
}
