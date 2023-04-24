package com.deskbtm.nawb.service.permanent;

import static android.content.Context.ALARM_SERVICE;
import static android.os.Build.VERSION.SDK_INT;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.AlarmManagerCompat;
import androidx.core.content.ContextCompat;

public class WatchdogReceiver extends BroadcastReceiver {

  private static final int QUEUE_REQUEST_ID = 2023423;
  private static final int DEFAULT_TRIGGER_DURATION = 5000;
  private static final String ACTION_RESPAWN = "com.deskbtm.nawb.service.permanent.WatchdogReceiver";


  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals(ACTION_RESPAWN)) {
      final Preferences preferences = new Preferences(context);
      if (preferences.isForeground()) {
        ContextCompat.startForegroundService(context, new Intent(context, PermanentService.class));
      } else {
        context.startService(new Intent(context, PermanentService.class));
      }
    }
  }

  public static void remove(Context context) {
    Intent intent = new Intent(context, WatchdogReceiver.class);
    intent.setAction(ACTION_RESPAWN);

    int flags = PendingIntent.FLAG_CANCEL_CURRENT;
    if (SDK_INT >= Build.VERSION_CODES.S) {
      flags |= PendingIntent.FLAG_MUTABLE;
    }

    PendingIntent pi = PendingIntent.getBroadcast(context, WatchdogReceiver.QUEUE_REQUEST_ID, intent, flags);
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    alarmManager.cancel(pi);
  }

  public static void add(Context context) {
    add(context, DEFAULT_TRIGGER_DURATION);
  }

  public static void add(Context context, int millis) {
    Intent intent = new Intent(context, WatchdogReceiver.class);
    intent.setAction(ACTION_RESPAWN);
    AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

    int flags = PendingIntent.FLAG_UPDATE_CURRENT;
    if (SDK_INT >= Build.VERSION_CODES.S) {
      flags |= PendingIntent.FLAG_MUTABLE;
    }

    PendingIntent pIntent = PendingIntent.getBroadcast(context, QUEUE_REQUEST_ID, intent, flags);
    // Check is background service every 5s (default)
    AlarmManagerCompat.setExact(manager, AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + millis, pIntent);
  }
}
