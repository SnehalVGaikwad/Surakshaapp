package com.suraksha.surakshaapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.suraksha.surakshaapp.R;
import com.suraksha.surakshaapp.Utils.PermissionManager;
import com.suraksha.surakshaapp.Utils.SharedPrefManager;

public class PermissionBlockedActivity extends AppCompatActivity {

    private Button btnRetry;
    private PermissionManager permissionManager;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_blocked);

        btnRetry = findViewById(R.id.btn_retry_permissions);
        permissionManager = new PermissionManager(this);
        prefManager = new SharedPrefManager(this);

        btnRetry.setOnClickListener(v -> {
            if (permissionManager.areAllPermissionsGranted()) {
                prefManager.setPermissionsGranted(true);
                startActivity(new Intent(PermissionBlockedActivity.this, HomeActivity.class));
                finish();
            } else {
                permissionManager.requestAllPermissions();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionManager.areAllPermissionsGranted()) {
            prefManager.setPermissionsGranted(true);
            startActivity(new Intent(PermissionBlockedActivity.this, HomeActivity.class));
            finish();
        }
    }
}
