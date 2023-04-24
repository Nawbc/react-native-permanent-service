package com.deskbtm.nawb.service.permanent;

import static android.os.Build.VERSION.SDK_INT;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class PermanentService extends Service {
  private static final String NAME = "PermanentService";
  public static volatile PowerManager.WakeLock wakeLock = null;
  final Map<Integer, IPermanentService> listeners = new HashMap<>();
  private Preferences preferences;
  private Handler mainHandler;
  private String notificationTitle;
  private String notificationContent;
  private String notificationChannelId;
  private int notificationId;
  AtomicBoolean running = new AtomicBoolean(false);

  private final IPermanentServiceBinder.Stub mPermanentServiceBinder = new IPermanentServiceBinder.Stub() {
    @Override
    public void invoke(String data) {
      try {

      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    public void bind(int id, IPermanentService service) throws RemoteException {
      synchronized (listeners) {
        listeners.put(id, service);
      }
    }

    @Override
    public void unBind(int id) throws RemoteException {
      synchronized (listeners) {
        listeners.remove(id);
      }
    }
  };

  @Override
  public void onCreate() {
    preferences = new Preferences(this);
    mainHandler = new Handler(Looper.getMainLooper());
    initNotification();
    updateNotification();
    super.onCreate();
  }

  @Override
  public void onDestroy() {
    stopForeground(true);
    running.set(false);

    super.onDestroy();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return mPermanentServiceBinder;
  }

  @Override
  public boolean onUnbind(Intent intent) {
    final int binderId = intent.getIntExtra("binder_id", 0);
    if (binderId != 0) {
      synchronized (listeners) {
        listeners.remove(binderId);
      }
    }

    return super.onUnbind(intent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    WatchdogReceiver.add(this);
    startService();
    return START_STICKY;
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    if (running.get()) {
      WatchdogReceiver.add(getApplicationContext(), 500);
    }
    super.onTaskRemoved(rootIntent);
  }


  @ReactMethod
  public void send(ReadableMap data) {
    try {
      synchronized (listeners) {
        for (Integer key : listeners.keySet()) {
          IPermanentService listener = listeners.get(key);
          if (listener != null) {
            listener.invoke(data.toString());
          }
        }
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  synchronized public static PowerManager.WakeLock getWakeLock(Context context) {
    if (wakeLock == null) {
      PowerManager powerManger = (PowerManager) context
        .getSystemService(Context.POWER_SERVICE);
      wakeLock = powerManger.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
        PermanentService.class.getName() + ".lock");
      wakeLock.setReferenceCounted(true);
    }

    return wakeLock;
  }

  private void sendEvent(ReactContext reactContext,
                         String event,
                         @Nullable WritableMap data) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(event, data);
  }

  private void startService() {
    try {
      if (running.get()) {
        Log.v(NAME, "Locked, service is running");
        return;
      }

      getWakeLock(getApplicationContext()).acquire();

      Log.d(NAME, "Start service");
      updateNotification();


    } catch (UnsatisfiedLinkError e) {
      notificationContent = "Error " + e.getMessage();
      updateNotification();
      Log.e(NAME, e.getMessage());
    }
  }

  private void initNotification() {
    String channelId = preferences.getNotificationChannelId();
    if (channelId == null) {
      notificationChannelId = Preferences.getName();
      createNotificationChannel();
    } else {
      notificationChannelId = channelId;
    }

    notificationContent = preferences.getNotificationContent();
    notificationId = preferences.getNotificationId();
    notificationTitle = preferences.getNotificationTitle();
  }

  private void createNotificationChannel() {
    if (SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = "Permanent background service";
      String description = "Executing process in background forever";

      int importance = NotificationManager.IMPORTANCE_LOW;
      NotificationChannel channel = new NotificationChannel(notificationChannelId, name, importance);
      channel.setDescription(description);

      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }

  protected void updateNotification() {
    if (preferences.isForeground()) {
      Intent intent;
      String deepLink = preferences.getNotificationDeeplink();
      int iconIdentifier = preferences.getIconIdentifier();

      if (deepLink == null) {
        String packageName = getApplicationContext().getPackageName();
        intent = getPackageManager().getLaunchIntentForPackage(packageName);
      } else {
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
      }

      int flags = PendingIntent.FLAG_CANCEL_CURRENT;

      if (SDK_INT >= Build.VERSION_CODES.S) {
        flags |= PendingIntent.FLAG_MUTABLE;
      }

      try {
        PendingIntent pendingIntent = PendingIntent.getActivity(PermanentService.this, 0, intent, flags);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, notificationChannelId)
          .setSmallIcon(iconIdentifier)
          .setAutoCancel(true)
          .setOngoing(true)
          .setContentTitle(notificationTitle)
          .setContentText(notificationContent)
          .setContentIntent(pendingIntent);

        startForeground(notificationId, mBuilder.build());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
