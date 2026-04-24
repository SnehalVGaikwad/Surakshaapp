package com.suraksha.surakshaapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.suraksha.surakshaapp.R;
import com.suraksha.surakshaapp.Utils.SharedPrefManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds
    private ImageView logoImage;
    private TextView appTitle;
    private FirebaseAuth mAuth;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logoImage = findViewById(R.id.splash_logo);
        appTitle = findViewById(R.id.splash_title);
        mAuth = FirebaseAuth.getInstance();
        prefManager = new SharedPrefManager(this);

        // Start fade-in animation
        startFadeInAnimation();

        // Navigate after splash duration
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> navigateNextScreen(),
                SPLASH_DURATION
        );
    }

    private void startFadeInAnimation() {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1500);
        logoImage.startAnimation(fadeIn);
        appTitle.startAnimation(fadeIn);
    }

    private void navigateNextScreen() {
        // Check if user is already logged in and permissions granted
        if (mAuth.getCurrentUser() != null && prefManager.isPermissionsGranted()) {
            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
        } else if (mAuth.getCurrentUser() != null && !prefManager.isPermissionsGranted()) {
            // User logged in but permissions not granted
            startActivity(new Intent(SplashActivity.this, PermissionBlockedActivity.class));
        } else if (prefManager.isRegistrationComplete()) {
            // Registration complete, go to login
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        } else {
            // New user, start registration flow
            startActivity(new Intent(SplashActivity.this, RegistrationActivity.class));
        }
        finish();
    }
}
