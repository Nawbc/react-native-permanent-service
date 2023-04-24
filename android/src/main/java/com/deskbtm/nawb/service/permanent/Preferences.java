package com.deskbtm.nawb.service.permanent;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
  private final SharedPreferences sp;
  private static final String NAME = "com.deskbtm.nawb.service.permanent";
  private static final String IS_AUTO_LAUNCH = "IS_AUTO_LAUNCH";
  private static final String IS_FOREGROUND = "IS_FOREGROUND";
  private static final String NOTIFICATION_TITLE = "NOTIFICATION_TITLE";
  private static final String NOTIFICATION_ID = "NOTIFICATION_ID";
  private static final String NOTIFICATION_CONTENT = "NOTIFICATION_CONTENT";
  private static final String NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_ID";
  private static final String NOTIFICATION_DEEPLINK = "NOTIFICATION_DEEPLINK";

  private static final String NOTIFICATION_ICON_IDENTIFIED = "NOTIFICATION_ICON_IDENTIFIED";
  private static int DEFAULT_NOTIFICATION_ID = 1236352;

  private boolean isAutoLaunch;
  private boolean isForeground;
  private String notificationTitle;
  private String notificationContent;
  private String notificationChannelId;
  private int notificationId;
  private String notificationDeepLink;
  private int iconIdentifier;

  public int getIconIdentifier() {
    return sp.getInt(NOTIFICATION_ICON_IDENTIFIED, 0);
  }

  public void setIconIdentifier(int iconIdentifier) {
    this.iconIdentifier = iconIdentifier;
    sp.edit().putInt(NOTIFICATION_ICON_IDENTIFIED, iconIdentifier).apply();
  }

  public String getNotificationDeeplink() {
    return this.sp.getString(NOTIFICATION_DEEPLINK, null);
  }

  public void setNotificationDeeplink(String notificationDeepLink) {
    this.notificationDeepLink = notificationDeepLink;
    sp.edit().putString(NOTIFICATION_DEEPLINK, notificationDeepLink).apply();
  }

  public static String getName() {
    return NAME;
  }

  public boolean isAutoLaunch() {
    return this.sp.getBoolean(IS_AUTO_LAUNCH, true);
  }

  public void setAutoLaunch(boolean autoLaunch) {
    isAutoLaunch = autoLaunch;
    sp.edit().putBoolean(IS_AUTO_LAUNCH, autoLaunch).apply();
  }

  public boolean isForeground() {
    return this.sp.getBoolean(IS_FOREGROUND, true);
  }

  public void setForeground(boolean foreground) {
    isForeground = foreground;
    sp.edit().putBoolean(IS_FOREGROUND, foreground).apply();
  }

  public String getNotificationTitle() {
    return sp.getString(NOTIFICATION_TITLE, "Permanent Service");
  }

  public void setNotificationTitle(String notificationTitle) {
    this.notificationTitle = notificationTitle;
    sp.edit().putString(NOTIFICATION_TITLE, notificationTitle).apply();
  }

  public String getNotificationContent() {
    return sp.getString(NOTIFICATION_CONTENT, "running");
  }

  public void setNotificationContent(String notificationContent) {
    this.notificationContent = notificationContent;
    sp.edit().putString(NOTIFICATION_CONTENT, notificationContent).apply();
  }

  public String getNotificationChannelId() {
    return sp.getString(NOTIFICATION_CHANNEL_ID, null);
  }

  public void setNotificationChannelId(String notificationChannelId) {
    this.notificationChannelId = notificationChannelId;
    sp.edit().putString(NOTIFICATION_CHANNEL_ID, notificationChannelId).apply();
  }

  public int getNotificationId() {
    return sp.getInt(NOTIFICATION_ID, DEFAULT_NOTIFICATION_ID);
  }

  public void setNotificationId(int notificationId) {
    this.notificationId = notificationId;
    sp.edit().putInt(NOTIFICATION_ID, notificationId).apply();
  }


  public Preferences(Context context) {
    this.sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
  }
}
