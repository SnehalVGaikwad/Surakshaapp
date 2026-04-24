package com.suraksha.surakshaapp.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationManager {
    private Activity activity;
    private FusedLocationProviderClient fusedLocationClient;

    public LocationManager(Activity activity) {
        this.activity = activity;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public void getCurrentLocation(LocationCallback callback) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            callback.onLocationReceived(location);
                        } else {
                            callback.onLocationReceived(null);
                        }
                    })
                    .addOnFailureListener(e -> callback.onLocationReceived(null));
        } else {
            callback.onLocationReceived(null);
        }
    }

    public interface LocationCallback {
        void onLocationReceived(Location location);
    }
}
