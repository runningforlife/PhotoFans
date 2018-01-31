package com.github.runningforlife.photosniffer.receiver;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;
import com.github.runningforlife.photosniffer.utils.WallpaperUtils;

/**
 * auto wallpaper alarm receiver
 */

public class WallpaperAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "WallpaperAlarmReceiver";

    public static final String ALARM_AUTO_WALLPAPER = "com.github.runningforlife.AUTO_WALLPAPER";
    private static final String ALARM_UPDATE_WALLPAPER_CACHE = "com.github.runningforlife.UPATE_WALLPAPER_CACHE";

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.v(TAG,"onReceive(): action=" + action);
        // start wallpaper alarm
        String autoWallpaper = context.getString(R.string.pref_automatic_wallpaper);
        boolean isAutoWallpaperEnabled = SharedPrefUtil.getBoolean(autoWallpaper, true);
        if (isAutoWallpaperEnabled && ALARM_AUTO_WALLPAPER.equals(action)) {
            WallpaperUtils.startAutoWallpaperAlarm(context);
            WallpaperUtils.setWallpaperFromCache(context, WallpaperManager.FLAG_SYSTEM);
        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            WallpaperUtils.startWallpaperSettingService(context);
            if (isAutoWallpaperEnabled) {
                WallpaperUtils.startAutoWallpaperAlarm(context);
            }
            // wallpaper cache alarm for OS < 21
            if (Build.VERSION.SDK_INT < 21) {
                WallpaperUtils.startWallpaperCacheUpdaterAlarm(context);
            }
        } else if (action.equals(ALARM_UPDATE_WALLPAPER_CACHE)) {
            // wifi connected or not
            String prefWifi = context.getString(R.string.pref_wifi_download);
            boolean isWifiMode = SharedPrefUtil.getBoolean(prefWifi, false);
            if ((isWifiMode && MiscUtil.isWifiConnected(context)) || MiscUtil.isConnected(context)) {
                WallpaperUtils.startWallpaperCacheUpdaterService(context);
            }

            WallpaperUtils.startWallpaperCacheUpdaterAlarm(context);
        }
    }
}
