package com.npairlines.ui.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonObject;
import com.npairlines.R;
import com.npairlines.data.SupabaseClient;
import com.npairlines.data.model.Booking;
import com.npairlines.data.repository.BookingRepository;
import com.npairlines.data.service.impl.AuthService;
import java.util.List;

public class MyBookingsFragment extends Fragment {

    private RecyclerView rvBookings;
    private BookingRepository repository;
    private BookingAdapter adapter;
    private LinearLayout loadingContainer;
    private LinearLayout emptyContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rvBookings = view.findViewById(R.id.rv_bookings);
        loadingContainer = view.findViewById(R.id.loading_container);
        emptyContainer = view.findViewById(R.id.empty_container);
        
        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new BookingAdapter();
        rvBookings.setAdapter(adapter);
        
        repository = new BookingRepository();
        loadBookings();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload bookings when returning to this fragment
        loadBookings();
    }

    private void loadBookings() {
        // Get real user ID from auth service
        String token = SupabaseClient.getInstance().getAccessToken();
        if (token == null) {
            showEmpty("Please login to view your trips");
            return;
        }
        
        showLoading(true);
        
        AuthService.getInstance().getUser(token, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(JsonObject userData) {
                String userId = userData.get("id").getAsString();
                
                repository.getUserBookings(userId, new BookingRepository.BookingsCallback() {
                    @Override
                    public void onSuccess(List<Booking> bookings) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            if (bookings == null || bookings.isEmpty()) {
                                showEmpty("No trips yet. Book a flight to see it here!");
                            } else {
                                emptyContainer.setVisibility(View.GONE);
                                rvBookings.setVisibility(View.VISIBLE);
                                adapter.setBookings(bookings);
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            showEmpty("Error loading trips: " + message);
                        });
                    }
                });
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showEmpty("Auth error: " + message);
                });
            }
        });
    }
    
    private void showLoading(boolean show) {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (show) {
            rvBookings.setVisibility(View.GONE);
            if (emptyContainer != null) {
                emptyContainer.setVisibility(View.GONE);
            }
        }
    }
    
    private void showEmpty(String message) {
        rvBookings.setVisibility(View.GONE);
        if (emptyContainer != null) {
            emptyContainer.setVisibility(View.VISIBLE);
            TextView tvEmpty = emptyContainer.findViewById(R.id.tv_empty);
            if (tvEmpty != null) {
                tvEmpty.setText(message);
            }
        }
    }
}
