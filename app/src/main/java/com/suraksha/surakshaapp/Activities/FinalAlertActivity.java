package com.suraksha.surakshaapp.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.suraksha.surakshaapp.R;
import com.suraksha.surakshaapp.Utils.LocationManager;

public class FinalAlertActivity extends AppCompatActivity {

    private TextView tvCountdown, tvWarning;
    private Button btnCancelSOS;

    private CountDownTimer timer;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String savedBackupPin = "";
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_alert);

        tvCountdown = findViewById(R.id.tv_final_countdown);
        tvWarning = findViewById(R.id.tv_final_warning);
        btnCancelSOS = findViewById(R.id.btn_cancel_sos);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadBackupPin();
        startAlarm();
        startFinalCountdown();

        btnCancelSOS.setOnClickListener(v -> showPinDialog());
    }

    private void loadBackupPin() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        savedBackupPin = documentSnapshot.getString("backup_pin");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load backup PIN",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void startAlarm() {
        mediaPlayer = MediaPlayer.create(
                this,
                android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
        );

        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    private void stopAlarm() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void startFinalCountdown() {
        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;

                tvCountdown.setText(String.valueOf(seconds));

                tvWarning.setText(
                        "SOS message will be sent to your emergency contacts in "
                                + seconds +
                                " seconds"
                );
            }

            @Override
            public void onFinish() {
                stopAlarm();

                Toast.makeText(
                        FinalAlertActivity.this,
                        "SOS TRIGGERED",
                        Toast.LENGTH_LONG
                ).show();

                triggerEmergencySOS();
            }
        }.start();
    }

    private void triggerEmergencySOS() {

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "User authentication failed",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (checkSelfPermission(android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(
                    this,
                    "SMS permission not granted",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        LocationManager locationManager = new LocationManager(this);

        locationManager.getCurrentLocation(location -> {

            if (location != null) {

                String mapsLink =
                        "https://www.google.com/maps?q="
                                + location.getLatitude()
                                + ","
                                + location.getLongitude();

                String message =
                        "SOS! I need help. My location: " + mapsLink;

                String userId = mAuth.getCurrentUser().getUid();

                db.collection("users")
                        .document(userId)
                        .collection("trusted_contacts")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {

                            for (QueryDocumentSnapshot document :
                                    queryDocumentSnapshots) {

                                String phone =
                                        document.getString("phone");

                                if (phone != null &&
                                        !phone.isEmpty()) {

                                    try {

                                        SmsManager smsManager;

                                        if (android.os.Build.VERSION.SDK_INT
                                                >= android.os.Build.VERSION_CODES.S) {

                                            smsManager =
                                                    getSystemService(SmsManager.class);

                                        } else {

                                            smsManager =
                                                    SmsManager.getDefault();
                                        }

                                        Toast.makeText(
                                                FinalAlertActivity.this,
                                                "Sending SOS to: " + phone,
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        ArrayList<String> parts = smsManager.divideMessage(message);

                                        smsManager.sendMultipartTextMessage(
                                                "+91" + phone,
                                                null,
                                                parts,
                                                null,
                                                null
                                        );

                                    } catch (Exception e) {

                                        Toast.makeText(
                                                FinalAlertActivity.this,
                                                "SMS failed for: "
                                                        + phone
                                                        + " | "
                                                        + e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                }
                            }

                            Toast.makeText(
                                    FinalAlertActivity.this,
                                    "Emergency SOS sent to all trusted contacts!",
                                    Toast.LENGTH_LONG
                            ).show();

                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(
                                        FinalAlertActivity.this,
                                        "Failed to fetch emergency contacts",
                                        Toast.LENGTH_LONG
                                ).show()
                        );

            } else {

                Toast.makeText(
                        FinalAlertActivity.this,
                        "Failed to fetch live location",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showPinDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle("Enter Backup PIN");

        builder.setMessage(
                "False alarm? Enter your backup PIN to cancel SOS."
        );

        final EditText input =
                new EditText(this);

        input.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );

        builder.setView(input);

        builder.setPositiveButton(
                "Verify",
                (dialog, which) -> {

                    String enteredPin =
                            input.getText()
                                    .toString()
                                    .trim();

                    if (enteredPin.isEmpty()) {

                        Toast.makeText(
                                this,
                                "Enter Backup PIN",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    if (enteredPin.equals(savedBackupPin)) {

                        if (timer != null) {
                            timer.cancel();
                        }

                        stopAlarm();

                        Toast.makeText(
                                this,
                                "SOS cancelled successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent =
                                new Intent(
                                        FinalAlertActivity.this,
                                        HomeActivity.class
                                );

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );

                        startActivity(intent);
                        finish();

                    } else {

                        Toast.makeText(
                                this,
                                "Incorrect Backup PIN",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

        builder.setNegativeButton(
                "Close",
                (dialog, which) ->
                        dialog.dismiss()
        );

        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
        }

        stopAlarm();
    }
}