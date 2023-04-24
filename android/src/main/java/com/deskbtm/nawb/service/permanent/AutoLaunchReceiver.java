package com.deskbtm.nawb.service.permanent;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

public class AutoLaunchReceiver extends BroadcastReceiver {

    final String QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON";

    @Override
    public void onReceive(Context context, Intent intent) {
        // ACTION_BOOT_COMPLETED cold
        // QUICKBOOT_POWERON reboot
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals(QUICKBOOT_POWERON)) {
            final Preferences preferences = new Preferences(context);
            if (preferences.isAutoLaunch()) {
                if (PermanentService.wakeLock == null) {
                    PermanentService.getWakeLock(context).acquire();
                }

                if (preferences.isForeground()) {
                    ContextCompat.startForegroundService(context, new Intent(context, PermanentService.class));
                } else {
                    context.startService(new Intent(context, PermanentService.class));
                }
            }
        }
    }
}
