package com.deskbtm.nawb.service.permanent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;

@ReactModule(name = PermanentServiceModule.NAME)
public class PermanentServiceModule extends ReactContextBaseJavaModule {
  public static final String NAME = "PermanentService";
  private final int binderId = (int) (System.nanoTime() & 0xfffffff);
  private Preferences preferences;
  private boolean mBound = false;
  private IPermanentServiceBinder mPermanentServiceBinder;
  private IPermanentService mPermanentService;

  private Handler mainHandler;

  private ReactContext reactContext;

  private static final int PERMISSION_CODE_ACCESS_NOTIFICATION_POLICY = 213;

  public PermanentServiceModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    preferences = new Preferences(reactContext);
  }

  private final ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      mPermanentServiceBinder = IPermanentServiceBinder.Stub.asInterface(service);

      try {
        mPermanentService = new IPermanentService.Stub() {
          @Override
          public void invoke(String data) {
            try {
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          @Override
          public void stop() {
//                        if (context != null && mPermanentServiceBinder != null) {
//                            mBound = false;
//                            context.unbindService(serviceConnection);
//                        }
          }
        };

        mPermanentServiceBinder.bind(binderId, mPermanentService);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      try {
        mBound = false;
        mPermanentServiceBinder.unBind(binderId);
        mPermanentService = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  };

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  public void initialize(ReadableMap config, Promise promise) {
    try {
      boolean isForeground = config.getBoolean("foreground");
      boolean isAutoLaunch = config.getBoolean("autoLaunch");
      ReadableMap notification = config.getMap("notification");
      int notificationId = notification.getInt("id");
      String notificationChannelId = notification.getString("channel");
      String notificationTitle = notification.getString("title");
      String notificationContent = notification.getString("content");
      String notificationIcon = notification.getString("icon");
      String notificationIconType = notification.getString("iconType");
      String notificationDeepLink = notification.getString("deeplink");

      if (notificationIcon == null) {
        notificationIcon = "ic_stat_clover";
        notificationIconType = "drawable";
      }

      final int iconIdentifier = reactContext.getResources().getIdentifier(notificationIcon, notificationIconType, reactContext.getPackageName());

      preferences.setForeground(isForeground);
      preferences.setAutoLaunch(isAutoLaunch);
      preferences.setNotificationId(notificationId);
      preferences.setNotificationChannelId(notificationChannelId);
      preferences.setNotificationTitle(notificationTitle);
      preferences.setNotificationContent(notificationContent);
      preferences.setNotificationDeeplink(notificationDeepLink);
      preferences.setIconIdentifier(iconIdentifier);

      promise.resolve(true);
    } catch (Exception e) {
      promise.reject("Init permanent service error", e);
    }
  }

  @ReactMethod
  public void start(Promise promise) {
    try {
      WatchdogReceiver.add(reactContext, 5000);
      boolean isForeground = preferences.isForeground();
      Intent intent = new Intent(reactContext, PermanentService.class);
      intent.putExtra("binderId", binderId);

      if (isForeground) {
        ContextCompat.startForegroundService(reactContext, intent);
      } else {
        reactContext.startService(intent);
      }

      mBound = reactContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
