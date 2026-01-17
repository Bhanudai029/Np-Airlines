package com.npairlines.ui.flight;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.npairlines.R;
import com.npairlines.data.model.Flight;
import java.util.ArrayList;
import java.util.List;

public class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.FlightViewHolder> {
    private List<Flight> flights = new ArrayList<>();
    private OnFlightClickListener listener;

    public void setFlights(List<Flight> flights) {
        this.flights = flights != null ? flights : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnFlightClickListener(OnFlightClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flight, parent, false);
        return new FlightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlightViewHolder holder, int position) {
        Flight flight = flights.get(position);
        holder.bind(flight);
    }

    @Override
    public int getItemCount() {
        return flights.size();
    }

    class FlightViewHolder extends RecyclerView.ViewHolder {
        TextView tvFlightNumber, tvAirline, tvDepartureTime, tvArrivalTime;
        TextView tvOrigin, tvDestination, tvDuration, tvPrice;

        public FlightViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFlightNumber = itemView.findViewById(R.id.tv_flight_number);
            tvAirline = itemView.findViewById(R.id.tv_airline);
            tvDepartureTime = itemView.findViewById(R.id.tv_departure_time);
            tvArrivalTime = itemView.findViewById(R.id.tv_arrival_time);
            tvOrigin = itemView.findViewById(R.id.tv_origin);
            tvDestination = itemView.findViewById(R.id.tv_destination);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvPrice = itemView.findViewById(R.id.tv_price);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onFlightClick(flights.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Flight flight) {
            tvFlightNumber.setText(flight.getFlightNumber());
            tvOrigin.setText(flight.getOriginCode());
            tvDestination.setText(flight.getDestinationCode());
            
            // Airline
            String airline = flight.getAirline();
            tvAirline.setText(airline != null ? airline : "Nepal Airlines");
            
            // Departure & Arrival Time (parse timestamp and show only time)
            String depTime = flight.getDepartureTime();
            String arrTime = flight.getArrivalTime();
            tvDepartureTime.setText(formatTime(depTime));
            tvArrivalTime.setText(formatTime(arrTime));
            
            // Duration
            int durationMins = flight.getDurationMins();
            if (durationMins > 0) {
                if (durationMins >= 60) {
                    int hours = durationMins / 60;
                    int mins = durationMins % 60;
                    tvDuration.setText(hours + "h " + mins + "m");
                } else {
                    tvDuration.setText(durationMins + "m");
                }
            } else {
                tvDuration.setText("--");
            }
            
            // Price in NPR
            double price = flight.getPriceEconomy();
            tvPrice.setText("NPR " + String.format("%,.0f", price));
        }
        
        private String formatTime(String timestamp) {
            if (timestamp == null || timestamp.isEmpty()) return "--:--";
            try {
                // Parse ISO timestamp like "2026-01-18 06:00:00+05:45"
                if (timestamp.contains(" ")) {
                    String timePart = timestamp.split(" ")[1];
                    if (timePart.contains(":")) {
                        String[] parts = timePart.split(":");
                        return parts[0] + ":" + parts[1];
                    }
                }
                // Fallback for other formats
                if (timestamp.contains("T")) {
                    String timePart = timestamp.split("T")[1];
                    if (timePart.contains(":")) {
                        String[] parts = timePart.split(":");
                        return parts[0] + ":" + parts[1];
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "--:--";
        }
    }

    public interface OnFlightClickListener {
        void onFlightClick(Flight flight);
    }
}
