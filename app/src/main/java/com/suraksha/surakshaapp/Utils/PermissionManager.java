package com.suraksha.surakshaapp.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private Activity activity;

    private static final String[] DANGEROUS_PERMISSIONS = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final String[] DANGEROUS_PERMISSIONS_TIRAMISU = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
    };

    public PermissionManager(Activity activity) {
        this.activity = activity;
    }

    public boolean areAllPermissionsGranted() {
        String[] permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                DANGEROUS_PERMISSIONS_TIRAMISU : DANGEROUS_PERMISSIONS;

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void requestAllPermissions() {
        String[] permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                DANGEROUS_PERMISSIONS_TIRAMISU : DANGEROUS_PERMISSIONS;

        ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
    }

    public static boolean isPermissionGranted(Activity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }
}
