package com.suraksha.surakshaapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.suraksha.surakshaapp.Models.UserProfile;
import com.suraksha.surakshaapp.R;
import com.suraksha.surakshaapp.Utils.EmailManager;
import com.suraksha.surakshaapp.Utils.FirebaseAuthManager;
import com.suraksha.surakshaapp.Utils.OTPManager;
import com.suraksha.surakshaapp.Utils.SharedPrefManager;
import com.suraksha.surakshaapp.Utils.ValidationUtils;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword;
    private TextInputLayout tilEmail, tilPhone, tilPassword;
    private Button btnVerifyEmail, btnVerifyOTP, btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseAuthManager authManager;
    private OTPManager otpManager;
    private EmailManager emailManager;
    private SharedPrefManager prefManager;

    private boolean emailVerified = false;
    private boolean phoneVerified = false;
    private String generatedOTP = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initializeViews();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        authManager = new FirebaseAuthManager(this);
        otpManager = new OTPManager();
        emailManager = new EmailManager(this);
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
        btnVerifyEmail = findViewById(R.id.btn_verify_email);
        btnVerifyOTP = findViewById(R.id.btn_verify_otp);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void setupListeners() {
        btnVerifyEmail.setOnClickListener(v -> handleEmailVerification());
        btnVerifyOTP.setOnClickListener(v -> handleOTPVerification());
        btnRegister.setOnClickListener(v -> handleRegistration());

        etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordValidation(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void handleEmailVerification() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !ValidationUtils.isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate and show verification code (mock for demo)
        String verificationCode = otpManager.generateOTP();
        Toast.makeText(this, "Verification code: " + verificationCode, Toast.LENGTH_LONG).show();

        // Store for comparison
        prefManager.setEmailVerificationCode(verificationCode);

        // Show input for code
        showEmailCodeInput();
    }

    private void showEmailCodeInput() {
        // In production, you'd show a dialog for user to input verification code
        // For now, we auto-verify after 2 seconds for demo
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            emailVerified = true;
            btnVerifyEmail.setText("✓ Email Verified");
            btnVerifyEmail.setEnabled(false);
            btnVerifyEmail.setTextColor(
                    getResources().getColor(android.R.color.holo_green_dark)
            );
            Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show();
            updateRegisterButton();
        }, 2000);
    }

    private void handleOTPVerification() {
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phone) || phone.length() != 10) {
            Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate OTP
        generatedOTP = otpManager.generateOTP();
        Toast.makeText(this, "OTP: " + generatedOTP, Toast.LENGTH_LONG).show();

        prefManager.setPhoneOTP(generatedOTP);

        // Show input for OTP
        showOTPInput();
    }

    private void showOTPInput() {
        // In production, show dialog for OTP input
        // For demo, auto-verify after 2 seconds
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            phoneVerified = true;
            btnVerifyOTP.setText("✓ Phone Verified");
            btnVerifyOTP.setEnabled(false);
            btnVerifyOTP.setTextColor(
                    getResources().getColor(android.R.color.holo_green_dark)
            );
            Toast.makeText(this, "Phone verified successfully!", Toast.LENGTH_SHORT).show();
            updateRegisterButton();
        }, 2000);
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
        btnRegister.setEnabled(
                emailVerified && phoneVerified && !etName.getText().toString().isEmpty()
                        && ValidationUtils.isStrongPassword(etPassword.getText().toString())
        );
    }

    private void handleRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!emailVerified || !phoneVerified) {
            Toast.makeText(this, "Please complete all verifications", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT).show();

        // Create user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Send email verification
                        mAuth.getCurrentUser().sendEmailVerification();

                        // Save user profile to Firestore
                        UserProfile userProfile = new UserProfile(name, email, phone);
                        db.collection("users").document(mAuth.getCurrentUser().getUid())
                                .set(userProfile)
                                .addOnSuccessListener(aVoid -> {
                                    prefManager.setRegistrationComplete(true);
                                    Toast.makeText(this, "Registration successful! Check email to verify.", Toast.LENGTH_SHORT).show();
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