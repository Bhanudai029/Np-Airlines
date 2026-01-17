package com.npairlines.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.npairlines.MainActivity;
import com.npairlines.R;

public class ConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        String ref = getIntent().getStringExtra("REFERENCE");
        if (ref == null) ref = "NP000000";

        TextView tvRef = findViewById(R.id.tv_reference);
        tvRef.setText(ref);
        
        // In a real app we'd pass route info here too
        // TextView tvRoute = findViewById(R.id.tv_route);
        // tvRoute.setText(getIntent().getStringExtra("ROUTE"));

        Button btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfirmationActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
