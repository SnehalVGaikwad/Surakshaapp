package com.suraksha.surakshaapp.Utils;

import android.app.Activity;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthManager {
    private FirebaseAuth mAuth;
    private Activity activity;

    public FirebaseAuthManager(Activity activity) {
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
    }

    public void registerUser(String email, String password, String name,
                             FirebaseAuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification();
                            callback.onSuccess("User registered. Verify email.");
                        }
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
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            callback.onSuccess("Login successful");
                        } else {
                            callback.onFailure("Email not verified");
                        }
                    } else {
                        callback.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "Login failed");
                    }
                });
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
}
