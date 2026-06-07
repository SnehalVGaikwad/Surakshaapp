package com.suraksha.surakshaapp.Utils;

import android.content.Context;
import android.os.PowerManager;

public class WakeLockHelper {

    public static void wakeScreen(Context context) {

        PowerManager powerManager =
                (PowerManager) context.getSystemService(
                        Context.POWER_SERVICE
                );

        if (powerManager == null) {
            return;
        }

        PowerManager.WakeLock wakeLock =
                powerManager.newWakeLock(
                        PowerManager.FULL_WAKE_LOCK
                                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                | PowerManager.ON_AFTER_RELEASE,
                        "Suraksha:WakeLock"
                );

        wakeLock.acquire(30000);
    }
}