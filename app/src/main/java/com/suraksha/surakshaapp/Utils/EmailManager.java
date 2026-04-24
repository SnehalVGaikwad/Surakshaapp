package com.suraksha.surakshaapp.Utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class EmailManager {
    private Activity activity;

    public EmailManager(Activity activity) {
        this.activity = activity;
    }

    public void sendVerificationEmail(String email, String verificationCode) {
        // In production, use Firebase Cloud Functions or backend API
        // For now, this is a placeholder
    }

    public void openEmailClient(String recipient, String subject, String body) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            activity.startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (android.content.ActivityNotFoundException ex) {
            // No email client available
        }
    }
}
