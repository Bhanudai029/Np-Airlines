package com.npairlines.ui.auth;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.npairlines.R;

public class TermsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        TextView tvContent = findViewById(R.id.tv_terms_content);
        
        String terms = "TERMS AND CONDITIONS OF CARRIAGE\n\n" +
                "1. DEFINITIONS\n" +
                "\"Carrier\" means Np Airlines.\n" +
                "\"Passenger\" means any person, except members of the crew, carried or to be carried in an aircraft pursuant to a Ticket.\n\n" +
                "2. LIABILITY FOR DAMAGE\n" +
                "The Carrier is liable for damage sustained in case of death or bodily injury of a passenger upon condition that the accident which caused the damage so sustained took place on board the aircraft or in the course of any of the operations of embarking or disembarking. Liability is limited in accordance with the laws of Nepal and applicable international conventions.\n\n" +
                "3. CHECKED BAGGAGE\n" +
                "Carrier is liable for damage to Checked Baggage. Liability is limited to US$20 per kilogram unless a higher value is declared in advance and additional charges are paid. Valuables, electronics, and medication should not be included in Checked Baggage.\n\n" +
                "4. SCHEDULES AND CANCELLATIONS\n" +
                "Carrier undertakes to use its best efforts to carry the passenger and baggage with reasonable dispatch. Times shown in timetables or elsewhere are not guaranteed and form no part of this contract. Carrier may without notice substitute alternate carriers or aircraft, and may alter or omit stopping places shown on the ticket in case of necessity. Schedules are subject to change without notice.\n\n" +
                "5. REFUNDS\n" +
                "Refunds will be made in accordance with the Carrier's regulations. Cancellation charges apply based on the timing of the cancellation relative to the flight departure time. 'No-Show' passengers may forfeit the fare entirely.\n\n" +
                "6. CHECK-IN\n" +
                "Passengers must check in at least 90 minutes before scheduled departure. Failure to check in on time may result in cancellation of the booking.\n\n" +
                "7. DANGEROUS GOODS\n" +
                "Passengers must not include in their baggage articles which are likely to endanger the aircraft or persons or property on board the aircraft.\n\n" +
                "8. GOVERNING LAW\n" +
                "These Terms & Conditions shall be governed by and construed in accordance with the laws of Nepal. Any dispute arising under these terms shall be subject to the exclusive jurisdiction of the courts of Nepal.";

        tvContent.setText(terms);
        
        findViewById(R.id.btn_close_terms).setOnClickListener(v -> finish());
    }
}
