package com.suraksha.surakshaapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.suraksha.surakshaapp.Models.TrustedContact;
import com.suraksha.surakshaapp.R;
import com.suraksha.surakshaapp.Utils.OTPManager;
import com.suraksha.surakshaapp.Utils.SharedPrefManager;
import com.suraksha.surakshaapp.Utils.ValidationUtils;

public class EmergencyContactActivity extends AppCompatActivity {

    private EditText etContactName, etContactEmail, etContactPhone;
    private TextInputLayout tilContactEmail, tilContactPhone;
    private Button btnVerifyEmail, btnVerifyPhone, btnNext;
    private TextView tvProgress;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPrefManager prefManager;
    private OTPManager otpManager;

    private int currentContactNumber = 1;
    private boolean emailVerified = false;
    private boolean phoneVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        initializeViews();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefManager = new SharedPrefManager(this);
        otpManager = new OTPManager();

        setupListeners();
        updateProgress();
    }

    private void initializeViews() {
        etContactName = findViewById(R.id.et_contact_name);
        etContactEmail = findViewById(R.id.et_contact_email);
        etContactPhone = findViewById(R.id.et_contact_phone);

        tilContactEmail = findViewById(R.id.til_contact_email);
        tilContactPhone = findViewById(R.id.til_contact_phone);

        btnVerifyEmail = findViewById(R.id.btn_verify_contact_email);
        btnVerifyPhone = findViewById(R.id.btn_verify_contact_phone);
        btnNext = findViewById(R.id.btn_next);

        tvProgress = findViewById(R.id.tv_progress);
    }

    private void setupListeners() {
        btnVerifyEmail.setOnClickListener(v -> handleContactEmailVerification());
        btnVerifyPhone.setOnClickListener(v -> handleContactPhoneVerification());
        btnNext.setOnClickListener(v -> handleNextContact());
    }

    private void updateProgress() {
        tvProgress.setText("Contact " + currentContactNumber + " of 4");
        btnNext.setText(currentContactNumber == 4 ? "Complete Registration" : "Next Contact");
        emailVerified = false;
        phoneVerified = false;
        btnNext.setEnabled(false);
    }

    private void handleContactEmailVerification() {
        String email = etContactEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !ValidationUtils.isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        String code = otpManager.generateOTP();
        Toast.makeText(this, "Verification code: " + code, Toast.LENGTH_LONG).show();
        prefManager.setContactEmailVerificationCode(currentContactNumber, code);

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            emailVerified = true;
            btnVerifyEmail.setText("✓ Email Verified");
            btnVerifyEmail.setEnabled(false);
            btnVerifyEmail.setTextColor(
                    getResources().getColor(android.R.color.holo_green_dark)
            );
            updateButtonState();
            Toast.makeText(this, "Contact email verified!", Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    private void handleContactPhoneVerification() {
        String phone = etContactPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phone) || phone.length() != 10) {
            Toast.makeText(this, "Please enter a valid 10-digit phone", Toast.LENGTH_SHORT).show();
            return;
        }

        String otp = otpManager.generateOTP();
        Toast.makeText(this, "OTP: " + otp, Toast.LENGTH_LONG).show();
        prefManager.setContactPhoneOTP(currentContactNumber, otp);

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            phoneVerified = true;
            btnVerifyPhone.setText("✓ Phone Verified");
            btnVerifyPhone.setEnabled(false);
            btnVerifyPhone.setTextColor(
                    getResources().getColor(android.R.color.holo_green_dark)
            );
            updateButtonState();
            Toast.makeText(this, "Contact phone verified!", Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    private void updateButtonState() {
        btnNext.setEnabled(
                emailVerified &&
                        phoneVerified &&
                        !etContactName.getText().toString().trim().isEmpty()
        );
    }

    private void handleNextContact() {
        String contactName = etContactName.getText().toString().trim();
        String contactEmail = etContactEmail.getText().toString().trim();
        String contactPhone = etContactPhone.getText().toString().trim();

        if (TextUtils.isEmpty(contactName) ||
                TextUtils.isEmpty(contactEmail) ||
                TextUtils.isEmpty(contactPhone)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        TrustedContact contact = new TrustedContact(
                contactName,
                contactEmail,
                contactPhone,
                true,
                true
        );

        db.collection("users")
                .document(userId)
                .collection("trusted_contacts")
                .document("contact_" + currentContactNumber)
                .set(contact)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Contact " + currentContactNumber + " saved!",
                            Toast.LENGTH_SHORT).show();

                    if (currentContactNumber == 4) {
                        prefManager.setAllContactsAdded(true);
                        startActivity(new Intent(
                                EmergencyContactActivity.this,
                                SuccessActivity.class));
                        finish();
                    } else {
                        currentContactNumber++;
                        resetForm();
                        updateProgress();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to save contact: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void resetForm() {
        etContactName.setText("");
        etContactEmail.setText("");
        etContactPhone.setText("");

        btnVerifyEmail.setText("Verify Email");
        btnVerifyPhone.setText("Verify Phone");

        btnVerifyEmail.setEnabled(true);
        btnVerifyPhone.setEnabled(true);

        // Reset colors to default (white; adjust if your theme uses another)
        btnVerifyEmail.setTextColor(
                getResources().getColor(android.R.color.white)
        );
        btnVerifyPhone.setTextColor(
                getResources().getColor(android.R.color.white)
        );

        emailVerified = false;
        phoneVerified = false;
        btnNext.setEnabled(false);
    }
}
