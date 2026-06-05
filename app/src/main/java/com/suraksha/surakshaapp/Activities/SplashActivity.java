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

        if (!prefManager.isRegistrationComplete()) {

            startActivity(new Intent(this, RegistrationActivity.class));

        } else if (!prefManager.isAllContactsAdded()) {

            startActivity(new Intent(this, EmergencyContactActivity.class));

        } else if (!prefManager.isBackupPinSet()) {

            startActivity(new Intent(this, BackupPinActivity.class));

        } else {

            startActivity(new Intent(this, HomeActivity.class));
        }

        finish();
    }
}
