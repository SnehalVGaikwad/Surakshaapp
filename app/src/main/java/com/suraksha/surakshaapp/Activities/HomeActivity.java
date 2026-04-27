package com.suraksha.surakshaapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.suraksha.surakshaapp.R;
import com.suraksha.surakshaapp.Utils.LocationManager;
import com.suraksha.surakshaapp.Utils.SharedPrefManager;
import com.suraksha.surakshaapp.Utils.SOSManager;

import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView btnSOS;
    private Button btnPolice, btnHospital;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SOSManager sosManager;
    private LocationManager locationManager;
    private SharedPrefManager prefManager;

    private CountDownTimer countDownTimer;

    // TEMP TEST MODE = 30 sec
    private final long START_TIME = 30000;

    private String savedBackupPin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this,
                    "User session expired. Please login again.",
                    Toast.LENGTH_LONG).show();

            startActivity(new Intent(
                    HomeActivity.this,
                    LoginActivity.class
            ));

            finish();
            return;
        }

        prefManager = new SharedPrefManager(this);
        sosManager = new SOSManager(this);
        locationManager = new LocationManager(this);

        loadBackupPin();

        setupListeners();
        startTimer();
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
        Toast.makeText(this,
                "Triggering SOS...",
                Toast.LENGTH_SHORT).show();

        locationManager.getCurrentLocation(location -> {
            if (location != null) {

                String mapsLink =
                        "https://www.google.com/maps?q="
                                + location.getLatitude()
                                + ","
                                + location.getLongitude();

                sosManager.triggerSOSAlert(
                        mAuth.getCurrentUser().getEmail(),
                        mapsLink
                );

                Toast.makeText(
                        HomeActivity.this,
                        "SOS Alert sent!",
                        Toast.LENGTH_SHORT
                ).show();

            } else {
                Toast.makeText(
                        HomeActivity.this,
                        "Unable to get location",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void callEmergency(String phoneNumber, String label) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED) {

            Intent callIntent =
                    new Intent(Intent.ACTION_CALL);

            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);

        } else {
            Toast.makeText(
                    this,
                    "Permission not granted for " + label,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(
                START_TIME,
                1000
        ) {
            @Override
            public void onTick(long millisUntilFinished) {

                int minutes =
                        (int) (millisUntilFinished / 1000) / 60;

                int seconds =
                        (int) (millisUntilFinished / 1000) % 60;

                String timeFormatted =
                        String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                minutes,
                                seconds
                        );

                btnSOS.setText(timeFormatted);
            }

            @Override
            public void onFinish() {

                Intent intent = new Intent(
                        HomeActivity.this,
                        SafetyCheckActivity.class
                );

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);
                finish();
            }
        }.start();
    }

    private void loadBackupPin() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        savedBackupPin =
                                documentSnapshot.getString("backup_pin");
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}