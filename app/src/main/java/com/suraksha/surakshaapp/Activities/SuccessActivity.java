package com.suraksha.surakshaapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.suraksha.surakshaapp.R;
import com.suraksha.surakshaapp.Utils.PermissionManager;
import com.suraksha.surakshaapp.Utils.SharedPrefManager;

public class SuccessActivity extends AppCompatActivity {

    private Button btnProceedToHome;
    private PermissionManager permissionManager;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        btnProceedToHome = findViewById(R.id.btn_proceed_to_home);
        permissionManager = new PermissionManager(this);
        prefManager = new SharedPrefManager(this);

        btnProceedToHome.setOnClickListener(v -> {
            if (permissionManager.areAllPermissionsGranted()) {
                prefManager.setPermissionsGranted(true);
                startActivity(new Intent(SuccessActivity.this, HomeActivity.class));
                finish();
            } else {
                startActivity(new Intent(SuccessActivity.this, PermissionBlockedActivity.class));
                finish();
            }
        });
    }
}
