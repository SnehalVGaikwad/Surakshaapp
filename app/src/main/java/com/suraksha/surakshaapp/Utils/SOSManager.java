package com.suraksha.surakshaapp.Utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.suraksha.surakshaapp.Models.SOSLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SOSManager {

    private final Activity activity;
    private final FirebaseFirestore db;
    private final SharedPrefManager prefManager;

    public SOSManager(Activity activity) {
        this.activity = activity;
        this.db = FirebaseFirestore.getInstance();
        this.prefManager = new SharedPrefManager(activity);
    }

    // Call from HomeActivity when SOS is pressed
    public void triggerSOSAlert(String userEmail, String mapsLink) {

        String timestamp = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        ).format(new Date());

        String userPhone = prefManager.getUserPhone();
        String userName = prefManager.getUserName();

        String alertMessage =
                "EMERGENCY ALERT – SURAKSHA APP\n" +
                        "User: " + userName + "\n" +
                        "Phone: " + userPhone + "\n" +
                        "Time: " + timestamp + "\n" +
                        "Location: " + mapsLink + "\n" +
                        "I am in danger. Please help.";

        // Log SOS once
        logSOSEvent(userPhone, mapsLink, timestamp);

        String userId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        // Get all trusted contacts and send alerts
        db.collection("users")
                .document(userId)
                .collection("trusted_contacts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(activity,
                                "No emergency contacts found",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String contactPhone = doc.getString("phone");
                        String contactEmail = doc.getString("email");

                        if (contactPhone != null && !contactPhone.isEmpty()) {
                            sendSOSSMS(contactPhone, alertMessage);
                            sendSOSWhatsApp(contactPhone, alertMessage);
                        }

                        if (contactEmail != null && !contactEmail.isEmpty()) {
                            sendSOSViaEmail(contactEmail, alertMessage, mapsLink);
                        }
                    }

                    Toast.makeText(activity,
                            "SOS triggered for all emergency contacts",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(activity,
                                "Failed to load contacts: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // TODO: implement actual HTTP call to your Cloud Function
    private void sendSOSViaEmail(String recipientEmail,
                                 String message,
                                 String mapsLink) {
        Toast.makeText(activity,
                "Sending SOS email to " + recipientEmail + "...",
                Toast.LENGTH_SHORT).show();
        // Use OkHttp here to call your HTTPS Cloud Function endpoint
    }

    // Automatic SMS with send & delivery status
    private void sendSOSSMS(String phoneNumber, String message) {
        String SENT = "SOS_SMS_SENT_" + phoneNumber;
        String DELIVERED = "SOS_SMS_DELIVERED_" + phoneNumber;

        PendingIntent sentPI = PendingIntent.getBroadcast(
                activity,
                0,
                new Intent(SENT),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent deliveredPI = PendingIntent.getBroadcast(
                activity,
                0,
                new Intent(DELIVERED),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Send status receiver
        BroadcastReceiver sentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context,
                                "SOS SMS sent to " + phoneNumber,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context,
                                "No service for " + phoneNumber,
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(context,
                                "Failed to send SMS to " + phoneNumber,
                                Toast.LENGTH_SHORT).show();
                        break;
                }
                activity.unregisterReceiver(this);
            }
        };

        // Delivery status receiver (optional)
        BroadcastReceiver deliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(context,
                            "SOS SMS delivered to " + phoneNumber,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context,
                            "SOS SMS NOT delivered to " + phoneNumber,
                            Toast.LENGTH_SHORT).show();
                }
                activity.unregisterReceiver(this);
            }
        };

        activity.registerReceiver(sentReceiver, new IntentFilter(SENT));
        activity.registerReceiver(deliveredReceiver, new IntentFilter(DELIVERED));

        try {
            SmsManager smsManager;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                smsManager = activity.getSystemService(SmsManager.class);
                if (smsManager == null) {
                    smsManager = SmsManager.getDefault();
                }
            } else {
                smsManager = SmsManager.getDefault();
            }

            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    sentPI,
                    deliveredPI
            );
        } catch (Exception e) {
            Toast.makeText(activity,
                    "Error sending SMS: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSOSWhatsApp(String phoneNumber, String message) {
        // Assuming Indian numbers saved as 10 digits
        String url = "https://wa.me/91" + phoneNumber +
                "?text=" + Uri.encode(message);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage("com.whatsapp");

        try {
            activity.startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(activity,
                    "WhatsApp not installed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void logSOSEvent(String userPhone,
                             String location,
                             String timestamp) {
        String userId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        SOSLog sosLog = new SOSLog(timestamp, userPhone, location);

        db.collection("users")
                .document(userId)
                .collection("sos_logs")
                .document(timestamp.replace(":", "-")
                        .replace(" ", "_"))
                .set(sosLog);
    }
}
