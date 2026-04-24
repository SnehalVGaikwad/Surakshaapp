package com.suraksha.surakshaapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.firestore.FirebaseFirestore;
import com.suraksha.surakshaapp.Models.UserProfile;
import com.suraksha.surakshaapp.R;
import com.suraksha.surakshaapp.Utils.FirebaseAuthManager;
import com.suraksha.surakshaapp.Utils.SharedPrefManager;
import com.suraksha.surakshaapp.Utils.ValidationUtils;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword;
    private TextInputLayout tilEmail, tilPhone, tilPassword;
    private Button btnVerifyOTP, btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseAuthManager authManager;
    private SharedPrefManager prefManager;

    private boolean phoneVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initializeViews();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        authManager = new FirebaseAuthManager(this);
        prefManager = new SharedPrefManager(this);

        setupListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        tilEmail = findViewById(R.id.til_email);
        tilPhone = findViewById(R.id.til_phone);
        tilPassword = findViewById(R.id.til_password);
        btnVerifyOTP = findViewById(R.id.btn_verify_otp);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void setupListeners() {
        btnVerifyOTP.setOnClickListener(v -> handleOTPVerification());
        btnRegister.setOnClickListener(v -> handleRegistration());

        etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordValidation(s.toString());
                updateRegisterButton();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateRegisterButton();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };
        etName.addTextChangedListener(watcher);
        etEmail.addTextChangedListener(watcher);
    }

    private void handleOTPVerification() {
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phone) || phone.length() != 10) {
            Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        btnVerifyOTP.setEnabled(false);
        btnVerifyOTP.setText("Sending OTP...");

        authManager.sendOtp(phone, new FirebaseAuthManager.PhoneAuthCallback() {
            @Override
            public void onCodeSent(String verificationId) {
                Toast.makeText(RegistrationActivity.this, "OTP sent to " + phone, Toast.LENGTH_SHORT).show();
                showOTPInputDialog();
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                markPhoneVerified();
            }

            @Override
            public void onFailure(String error) {
                btnVerifyOTP.setEnabled(true);
                btnVerifyOTP.setText("Verify Phone (OTP)");
                Toast.makeText(RegistrationActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showOTPInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter OTP");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty()) {
                verifyCode(code);
            } else {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show();
                btnVerifyOTP.setEnabled(true);
                btnVerifyOTP.setText("Verify Phone (OTP)");
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            btnVerifyOTP.setEnabled(true);
            btnVerifyOTP.setText("Verify Phone (OTP)");
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
                btnVerifyOTP.setEnabled(true);
                btnVerifyOTP.setText("Verify Phone (OTP)");
                Toast.makeText(RegistrationActivity.this, "Verification failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markPhoneVerified() {
        phoneVerified = true;
        btnVerifyOTP.setText("✓ Phone Verified");
        btnVerifyOTP.setEnabled(false);
        btnVerifyOTP.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        Toast.makeText(this, "Phone verified successfully!", Toast.LENGTH_SHORT).show();
        updateRegisterButton();
    }

    private void updatePasswordValidation(String password) {
        boolean isValid = ValidationUtils.isStrongPassword(password);
        if (isValid) {
            tilPassword.setHelperText("Password is strong");
        } else {
            tilPassword.setHelperText("Min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char");
        }
    }

    private void updateRegisterButton() {
        String name = etName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        btnRegister.setEnabled(
                phoneVerified && 
                !name.isEmpty() && 
                ValidationUtils.isValidEmail(email) &&
                ValidationUtils.isStrongPassword(password)
        );
    }

    private void handleRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!phoneVerified) {
            Toast.makeText(this, "Please verify your phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT).show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserProfile userProfile = new UserProfile(name, email, phone);
                        db.collection("users").document(mAuth.getCurrentUser().getUid())
                                .set(userProfile)
                                .addOnSuccessListener(aVoid -> {
                                    prefManager.setRegistrationComplete(true);
                                    prefManager.setUserName(name);
                                    prefManager.setUserPhone(phone);
                                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegistrationActivity.this, EmergencyContactActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    btnRegister.setEnabled(true);
                                });
                    } else {
                        Toast.makeText(this,
                                "Registration failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                        btnRegister.setEnabled(true);
                    }
                });
    }
}