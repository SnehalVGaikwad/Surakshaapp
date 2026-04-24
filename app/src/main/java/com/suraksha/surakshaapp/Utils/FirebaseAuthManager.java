package com.suraksha.surakshaapp.Utils;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class FirebaseAuthManager {
    private FirebaseAuth mAuth;
    private Activity activity;
    private String verificationId;

    public FirebaseAuthManager(Activity activity) {
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
    }

    public void registerUser(String email, String password, String name,
                             FirebaseAuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("User registered successfully.");
                    } else {
                        callback.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "Registration failed");
                    }
                });
    }

    public void loginUser(String email, String password, FirebaseAuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Login successful");
                    } else {
                        callback.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "Login failed");
                    }
                });
    }

    public void sendOtp(String phoneNumber, PhoneAuthCallback callback) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91" + phoneNumber) // Assuming Indian numbers, adjust as needed
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                callback.onVerificationCompleted(credential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                callback.onFailure(e.getMessage());
                            }

                            @Override
                            public void onCodeSent(@NonNull String verId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                verificationId = verId;
                                callback.onCodeSent(verId);
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void verifyOtp(String code, PhoneAuthCallback callback) {
        if (verificationId == null) {
            callback.onFailure("Verification ID is null");
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        callback.onVerificationCompleted(credential);
    }

    public void logoutUser() {
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public interface FirebaseAuthCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public interface PhoneAuthCallback {
        void onCodeSent(String verificationId);
        void onVerificationCompleted(PhoneAuthCredential credential);
        void onFailure(String error);
    }
}
