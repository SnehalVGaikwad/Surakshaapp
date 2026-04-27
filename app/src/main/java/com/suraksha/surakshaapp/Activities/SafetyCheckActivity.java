package com.suraksha.surakshaapp.Activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.suraksha.surakshaapp.R;

public class SafetyCheckActivity extends AppCompatActivity {

    private TextView tvCountdown;
    private Button btnImSafe;

    private CountDownTimer timer;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_check);

        tvCountdown = findViewById(R.id.tv_countdown);
        btnImSafe = findViewById(R.id.btn_im_safe);

        startAlarm();
        startSafetyCountdown();

        btnImSafe.setOnClickListener(v -> {

            // Stop countdown
            if (timer != null) {
                timer.cancel();
            }

            // Stop alarm
            stopAlarm();

            // Restart home fresh
            Intent intent = new Intent(
                    SafetyCheckActivity.this,
                    HomeActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
    }

    private void startAlarm() {
        mediaPlayer = MediaPlayer.create(
                this,
                android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
        );

        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    private void stopAlarm() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void startSafetyCountdown() {
        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(
                        String.valueOf(millisUntilFinished / 1000)
                );
            }

            @Override
            public void onFinish() {
                stopAlarm();

                Intent intent = new Intent(
                        SafetyCheckActivity.this,
                        FinalAlertActivity.class
                );

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);
                finish();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
        }

        stopAlarm();
    }
}