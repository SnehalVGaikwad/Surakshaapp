package com.suraksha.surakshaapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.suraksha.surakshaapp.R;
import com.suraksha.surakshaapp.Utils.LocationManager;
import com.suraksha.surakshaapp.Utils.SharedPrefManager;
import com.suraksha.surakshaapp.Utils.SOSManager;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    private TextView btnSOS;
    private Button btnPolice, btnHospital;
    private FirebaseAuth mAuth;
    private SOSManager sosManager;
    private LocationManager locationManager;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        mAuth = FirebaseAuth.getInstance();
        sosManager = new SOSManager(this);
        locationManager = new LocationManager(this);
        prefManager = new SharedPrefManager(this);

        setupListeners();
    }

    private void initializeViews() {
        btnSOS = findViewById(R.id.btn_sos);
        btnPolice = findViewById(R.id.btn_call_police);
        btnHospital = findViewById(R.id.btn_call_ambulance);
    }

    private void setupListeners() {
        btnSOS.setOnClickListener(v -> handleSOS());
        btnPolice.setOnClickListener(v -> callEmergency("100", "Police"));
        btnHospital.setOnClickListener(v -> callEmergency("102", "Ambulance"));
    }

    private void handleSOS() {
        Toast.makeText(this, "Triggering SOS...", Toast.LENGTH_SHORT).show();

        // Get current location
        locationManager.getCurrentLocation(location -> {
            if (location != null) {
                // Format location as Google Maps link
                String mapsLink = "https://www.google.com/maps?q=" +
                        location.getLatitude() + "," + location.getLongitude();

                // Trigger SOS alerts
                sosManager.triggerSOSAlert(
                        mAuth.getCurrentUser().getEmail(),
                        mapsLink
                );

                Toast.makeText(HomeActivity.this, "SOS Alert sent!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HomeActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callEmergency(String phoneNumber, String label) {
        // Check permission before calling
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } else {
            Toast.makeText(this, "Permission not granted for " + label, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogout() {
        mAuth.signOut();
        prefManager.clearAllData();
        startActivity(new Intent(HomeActivity.this, SplashActivity.class));
        finish();
    }
}
