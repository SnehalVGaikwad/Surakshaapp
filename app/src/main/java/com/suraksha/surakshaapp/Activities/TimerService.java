package com.suraksha.surakshaapp.Activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.suraksha.surakshaapp.R;

import java.util.Locale;

public class TimerService extends Service {

    private static final String CHANNEL_ID = "timer_channel";
    private static final int NOTIFICATION_ID = 1;

    private CountDownTimer timer;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        // Start foreground immediately
        NotificationCompat.Builder initialNotification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("SOS Countdown Active")
                        .setContentText("Starting timer...")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setOngoing(true);

        startForeground(NOTIFICATION_ID, initialNotification.build());

        // Start countdown timer
        timer = new CountDownTimer(180000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;

                String time = String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        minutes,
                        seconds
                );

                NotificationCompat.Builder notification =
                        new NotificationCompat.Builder(TimerService.this, CHANNEL_ID)
                                .setContentTitle("SOS Countdown Active")
                                .setContentText("Time Remaining: " + time)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setOngoing(true);

                NotificationManager manager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                if (manager != null) {
                    manager.notify(NOTIFICATION_ID, notification.build());
                }
            }

            @Override
            public void onFinish() {
                NotificationCompat.Builder notification =
                        new NotificationCompat.Builder(TimerService.this, CHANNEL_ID)
                                .setContentTitle("SOS Timer Finished")
                                .setContentText("Emergency timer ended")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setOngoing(false);

                NotificationManager manager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                if (manager != null) {
                    manager.notify(NOTIFICATION_ID, notification.build());
                }

                stopSelf();
            }
        }.start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "SOS Timer Service",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(android.content.Intent intent) {
        return null;
    }
}