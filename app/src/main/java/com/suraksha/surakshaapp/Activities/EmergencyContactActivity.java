package com.suraksha.surakshaapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.firestore.FirebaseFirestore;
import com.suraksha.surakshaapp.Models.TrustedContact;
import com.suraksha.surakshaapp.R;
import com.suraksha.surakshaapp.Utils.FirebaseAuthManager;
import com.suraksha.surakshaapp.Utils.SharedPrefManager;
import com.suraksha.surakshaapp.Utils.ValidationUtils;

public class EmergencyContactActivity extends AppCompatActivity {

    private EditText etContactName, etContactEmail, etContactPhone;
    private TextInputLayout tilContactEmail, tilContactPhone;
    private Button btnVerifyPhone, btnNext;
    private TextView tvProgress;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPrefManager prefManager;
    private FirebaseAuthManager authManager;

    private int currentContactNumber = 1;
    private boolean phoneVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        initializeViews();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefManager = new SharedPrefManager(this);
        authManager = new FirebaseAuthManager(this);

        setupListeners();
        updateProgress();
    }

    private void initializeViews() {
        etContactName = findViewById(R.id.et_contact_name);
        etContactEmail = findViewById(R.id.et_contact_email);
        etContactPhone = findViewById(R.id.et_contact_phone);

        tilContactEmail = findViewById(R.id.til_contact_email);
        tilContactPhone = findViewById(R.id.til_contact_phone);

        btnVerifyPhone = findViewById(R.id.btn_verify_contact_phone);
        btnNext = findViewById(R.id.btn_next);

        tvProgress = findViewById(R.id.tv_progress);
    }

    private void setupListeners() {
        btnVerifyPhone.setOnClickListener(v -> handleContactPhoneVerification());
        btnNext.setOnClickListener(v -> handleNextContact());

        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };
        etContactName.addTextChangedListener(watcher);
        etContactEmail.addTextChangedListener(watcher);
    }

    private void updateProgress() {
        tvProgress.setText("Contact " + currentContactNumber + " of 4");
        btnNext.setText(currentContactNumber == 4 ? "Complete Registration" : "Next Contact");
        phoneVerified = false;
        btnNext.setEnabled(false);
    }

    private void handleContactPhoneVerification() {
        String phone = etContactPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phone) || phone.length() != 10) {
            Toast.makeText(this, "Please enter a valid 10-digit phone", Toast.LENGTH_SHORT).show();
            return;
        }

         btnVerifyPhone.setEnabled(false);
        btnVerifyPhone.setText("Sending OTP...");

        authManager.sendOtp(phone, new FirebaseAuthManager.PhoneAuthCallback() {
            @Override
            public void onCodeSent(String verificationId) {
                Toast.makeText(EmergencyContactActivity.this, "OTP sent to " + phone, Toast.LENGTH_SHORT).show();
                showOTPInputDialog();
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                markPhoneVerified();
            }

            @Override
            public void onFailure(String error) {
                btnVerifyPhone.setEnabled(true);
                btnVerifyPhone.setText("Verify Phone");
                Toast.makeText(EmergencyContactActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showOTPInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify Contact Phone");
        builder.setMessage("Enter the OTP sent to the contact's phone number");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty()) {
                verifyCode(code);
            } else {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show();
                btnVerifyPhone.setEnabled(true);
                btnVerifyPhone.setText("Verify Phone");
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            btnVerifyPhone.setEnabled(true);
            btnVerifyPhone.setText("Verify Phone");
        });

        builder.show();
    }

    private void verifyCode(String code) {
        authManager.verifyOtp(code, new FirebaseAuthManager.PhoneAuthCallback() {
            @Override
            public void onCodeSent(String verificationId) {}

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                markPhoneVerified();
            }

            @Override
            public void onFailure(String error) {
                btnVerifyPhone.setEnabled(true);
                btnVerifyPhone.setText("Verify Phone");
                Toast.makeText(EmergencyContactActivity.this, "Verification failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markPhoneVerified() {
        phoneVerified = true;
        btnVerifyPhone.setText("✓ Phone Verified");
        btnVerifyPhone.setEnabled(false);
        btnVerifyPhone.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        Toast.makeText(this, "Contact phone verified!", Toast.LENGTH_SHORT).show();
        updateButtonState();
    }

    private void updateButtonState() {
        String email = etContactEmail.getText().toString().trim();
        btnNext.setEnabled(
                phoneVerified &&
                        !etContactName.getText().toString().trim().isEmpty() &&
                        ValidationUtils.isValidEmail(email)
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
                true, // verified_phone
                true  // verified_email (auto-verified)
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
                                BackupPinActivity.class));
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

        btnVerifyPhone.setText("Verify Phone");
        btnVerifyPhone.setEnabled(true);
        btnVerifyPhone.setTextColor(
                getResources().getColor(android.R.color.white)
        );

        phoneVerified = false;
        btnNext.setEnabled(false);
    }
}
