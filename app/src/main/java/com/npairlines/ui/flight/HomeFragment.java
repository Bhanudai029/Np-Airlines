package com.npairlines.ui.flight;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.npairlines.R;
import com.npairlines.ui.seat.SeatSelectionActivity;

public class HomeFragment extends Fragment {

    private TextView tvOrigin, tvDestination, tvAirline, tvPassengers, tvDate;
    private Button btnSearch;
    private RecyclerView rvFlights;
    private LinearLayout loadingContainer, emptyContainer;
    private TextView tvLoadingText, tvLoadingSubtitle;
    private FlightSearchViewModel viewModel;
    private FlightAdapter adapter;
    private Handler loadingTextHandler;
    private Runnable loadingTextRunnable;

    private final String[] NEPAL_CITIES = {
        "Kathmandu", "Pokhara", "Biratnagar", "Bhairahawa", "Nepalgunj", "Bharatpur", "Janakpur", "Dhangadhi", "Simara"
    };

    private final String[] ACTIVE_AIRLINES = {
        "Any Airline", "Buddha Air", "Yeti Airlines", "Shree Airlines", "Sita Air", "Summit Air", "Tara Air", "Nepal Airlines"
    };

    private final String[] PASSENGER_COUNTS = {
        "1 Passenger", "2 Passengers", "3 Passengers", "4 Passengers", "5 Passengers", "Custom"
    };
    
    private final String[] LOADING_TEXTS = {
        "Searching flights...",
        "Checking availability...",
        "Finding best prices...",
        "Almost there..."
    };
    
    private final String[] LOADING_SUBTITLES = {
        "Finding best routes",
        "Comparing airlines",
        "Optimizing results",
        "Just a moment"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvOrigin = view.findViewById(R.id.tv_origin);
        tvDestination = view.findViewById(R.id.tv_destination);
        tvAirline = view.findViewById(R.id.tv_airline);
        tvPassengers = view.findViewById(R.id.tv_passengers);
        btnSearch = view.findViewById(R.id.btn_search);
        rvFlights = view.findViewById(R.id.rv_flights);
        loadingContainer = view.findViewById(R.id.loading_container);
        emptyContainer = view.findViewById(R.id.empty_container);
        tvLoadingText = view.findViewById(R.id.tv_loading_text);
        tvLoadingSubtitle = view.findViewById(R.id.tv_loading_subtitle);
        tvDate = view.findViewById(R.id.tv_date);

        adapter = new FlightAdapter();
        rvFlights.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFlights.setAdapter(adapter);

        viewModel = new FlightSearchViewModel();
        
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });
        
        viewModel.getFlights().observe(getViewLifecycleOwner(), flights -> {
            adapter.setFlights(flights);
            if (flights != null && !flights.isEmpty()) {
                emptyContainer.setVisibility(View.GONE);
                rvFlights.setVisibility(View.VISIBLE);
            } else if (flights != null) {
                // Search completed but no results
                emptyContainer.setVisibility(View.VISIBLE);
                rvFlights.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                android.widget.Toast.makeText(getContext(), "Search error: " + error, android.widget.Toast.LENGTH_LONG).show();
                android.util.Log.e("HomeFragment", "Flight search error: " + error);
            }
        });

        tvOrigin.setOnClickListener(v -> showSelectionDialog(tvOrigin, "From", NEPAL_CITIES));
        tvDestination.setOnClickListener(v -> showSelectionDialog(tvDestination, "To", NEPAL_CITIES));
        tvAirline.setOnClickListener(v -> showSelectionDialog(tvAirline, "Select Airline", ACTIVE_AIRLINES));
        tvPassengers.setOnClickListener(v -> showPassengerSelectionDialog());
        tvDate.setOnClickListener(v -> showDatePicker());

        btnSearch.setOnClickListener(v -> performSearch());
        
        adapter.setOnFlightClickListener(flight -> {
            Intent intent = new Intent(getActivity(), SeatSelectionActivity.class);
            intent.putExtra("FLIGHT_ID", flight.getId());
            
            // Parse for passenger count
            String passengerText = tvPassengers.getText().toString(); // e.g., "1 Passenger", "2 Passengers" or just "2"
            int count = 1;
            try {
                String numberOnly = passengerText.replaceAll("[^0-9]", "");
                if (!numberOnly.isEmpty()) {
                    count = Integer.parseInt(numberOnly);
                }
            } catch (Exception e) {
                count = 1;
            }
            intent.putExtra("PASSENGER_COUNT", count);
            
            startActivity(intent);
        });
    }
    
    private void showLoading() {
        loadingContainer.setVisibility(View.VISIBLE);
        rvFlights.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.GONE);
        btnSearch.setEnabled(false);
        btnSearch.setText("Searching...");
        
        // Animate loading text changes
        startLoadingTextAnimation();
        
        // Pulse animation on the container
        ObjectAnimator pulseAnim = ObjectAnimator.ofFloat(loadingContainer, "alpha", 0.7f, 1f);
        pulseAnim.setDuration(800);
        pulseAnim.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnim.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnim.setInterpolator(new LinearInterpolator());
        pulseAnim.start();
    }
    
    private void hideLoading() {
        loadingContainer.setVisibility(View.GONE);
        btnSearch.setEnabled(true);
        btnSearch.setText(R.string.action_search);
        
        // Stop text animation
        stopLoadingTextAnimation();
    }
    
    private int loadingTextIndex = 0;
    
    private void startLoadingTextAnimation() {
        loadingTextIndex = 0;
        loadingTextHandler = new Handler(Looper.getMainLooper());
        loadingTextRunnable = new Runnable() {
            @Override
            public void run() {
                if (tvLoadingText != null && tvLoadingSubtitle != null) {
                    // Fade out
                    tvLoadingText.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                        loadingTextIndex = (loadingTextIndex + 1) % LOADING_TEXTS.length;
                        tvLoadingText.setText(LOADING_TEXTS[loadingTextIndex]);
                        tvLoadingSubtitle.setText(LOADING_SUBTITLES[loadingTextIndex]);
                        // Fade in
                        tvLoadingText.animate().alpha(1f).setDuration(200).start();
                        tvLoadingSubtitle.animate().alpha(1f).setDuration(200).start();
                    }).start();
                    tvLoadingSubtitle.animate().alpha(0f).setDuration(200).start();
                }
                loadingTextHandler.postDelayed(this, 1500);
            }
        };
        loadingTextHandler.postDelayed(loadingTextRunnable, 1500);
    }
    
    private void stopLoadingTextAnimation() {
        if (loadingTextHandler != null && loadingTextRunnable != null) {
            loadingTextHandler.removeCallbacks(loadingTextRunnable);
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLoadingTextAnimation();
    }

    private void showSelectionDialog(TextView targetView, String title, String[] items) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setItems(items, (dialog, which) -> {
                targetView.setText(items[which]);
            })
            .show();
    }
    
    private void showDatePicker() {
        final java.util.Calendar c = java.util.Calendar.getInstance();
        int year = c.get(java.util.Calendar.YEAR);
        int month = c.get(java.util.Calendar.MONTH);
        int day = c.get(java.util.Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    String selectedDate = dayOfMonth + " " + months[monthOfYear] + " " + year1;
                    tvDate.setText(selectedDate);
                }, year, month, day);
        
        // Set min date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        // Set max date to 3 months from now
        java.util.Calendar maxDate = java.util.Calendar.getInstance();
        maxDate.add(java.util.Calendar.MONTH, 3);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        
        datePickerDialog.show();
    }

    private void showPassengerSelectionDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Passengers")
                .setItems(PASSENGER_COUNTS, (dialog, which) -> {
                    String selected = PASSENGER_COUNTS[which];
                    if (selected.equals("Custom")) {
                        showCustomPassengerInput();
                    } else {
                        tvPassengers.setText(selected);
                    }
                })
                .show();
    }

    private void showCustomPassengerInput() {
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        
        android.widget.FrameLayout container = new android.widget.FrameLayout(requireContext());
        android.widget.FrameLayout.LayoutParams params = new  android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = padding;
        params.rightMargin = padding;
        
        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter number (1-25)");
        container.setPadding(padding, 0, padding, 0);
        container.addView(input);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Enter Passengers")
                .setView(container)
                .setPositiveButton("OK", (dialog, which) -> {
                    String val = input.getText().toString();
                    if (!val.isEmpty()) {
                        try {
                            int count = Integer.parseInt(val);
                            if (count >= 1 && count <= 25) {
                                tvPassengers.setText(count + (count == 1 ? " Passenger" : " Passengers"));
                            } else {
                                android.widget.Toast.makeText(getContext(), "Please enter 1-25 only", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            android.widget.Toast.makeText(getContext(), "Invalid number", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performSearch() {
        String origin = tvOrigin.getText().toString();
        String destination = tvDestination.getText().toString();
        String airline = tvAirline.getText().toString();
        String passengers = tvPassengers.getText().toString();
        
        if (origin.equals("From") 
                || destination.equals("To") 
                || airline.equals("Select Airline")
                || tvDate.getText().toString().equals("Select Date")) {
             android.widget.Toast.makeText(getContext(), "Please fill all fields including date", android.widget.Toast.LENGTH_SHORT).show();
             return;
        }
        
        if (origin.equals(destination)) {
            android.widget.Toast.makeText(getContext(), "Origin and destination cannot be the same", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        String originCode = getAirportCode(origin);
        String destCode = getAirportCode(destination);
        
        // Pass codes and airline to ViewModel
        viewModel.search(originCode, destCode, airline);
    }

    private String getAirportCode(String city) {
        switch (city) {
            case "Kathmandu": return "KTM";
            case "Pokhara": return "PKR";
            case "Biratnagar": return "BIR";
            case "Bhairahawa": return "BWA";
            case "Nepalgunj": return "KEP";
            case "Bharatpur": return "BHR";
            case "Janakpur": return "JKR";
            case "Dhangadhi": return "DHI";
            case "Simara": return "SIF";
            default: return city; // Fallback
        }
    }
}
