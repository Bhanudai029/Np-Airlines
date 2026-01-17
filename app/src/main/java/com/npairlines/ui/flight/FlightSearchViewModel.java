package com.npairlines.ui.flight;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.npairlines.data.model.Flight;
import com.npairlines.data.service.impl.FlightService;
import com.npairlines.ui.base.BaseViewModel;
import java.util.ArrayList;
import java.util.List;

public class FlightSearchViewModel extends BaseViewModel {
    private final MutableLiveData<List<Flight>> flights = new MutableLiveData<>();
    private FlightService service;

    public FlightSearchViewModel() {
        service = FlightService.getInstance();
    }

    public LiveData<List<Flight>> getFlights() { return flights; }

    public void search(String origin, String destination, String airline) {
        setLoading(true);
        service.searchFlights(origin, destination, new FlightService.FlightCallback() {
            @Override
            public void onSuccess(List<Flight> flightList) {
                setLoading(false);
                
                // Filter by airline if specified (not "Any Airline")
                if (airline != null && !airline.isEmpty() && !airline.equals("Any Airline")) {
                    List<Flight> filteredFlights = new ArrayList<>();
                    for (Flight f : flightList) {
                        if (f.getAirline() != null && f.getAirline().equals(airline)) {
                            filteredFlights.add(f);
                        }
                    }
                    flights.setValue(filteredFlights);
                } else {
                    flights.setValue(flightList);
                }
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                setError(message);
            }
        });
    }
}
