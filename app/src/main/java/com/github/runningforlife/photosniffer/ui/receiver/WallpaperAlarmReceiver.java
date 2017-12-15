package com.github.runningforlife.photosniffer.ui.receiver;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.service.LockScreenUpdateService;
import com.github.runningforlife.photosniffer.service.MyThreadFactory;
import com.github.runningforlife.photosniffer.service.WallpaperCacheService;
import com.github.runningforlife.photosniffer.service.WallpaperUpdaterService;
import com.github.runningforlife.photosniffer.utils.DisplayUtil;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;
import com.github.runningforlife.photosniffer.utils.WallpaperUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.JOB_SCHEDULER_SERVICE;

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
           new Thread(new Runnable() {
                @Override
                public void run() {
                    WallpaperUtils.setWallpaperFromCache(context, WallpaperManager.FLAG_SYSTEM);
                }
            }).start();
        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)){
            if (Build.VERSION.SDK_INT >= 24) {
                WallpaperUtils.startLockScreenWallpaperService(context);
            }
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
            // make sure that lock screen wallpaper service is active
            WallpaperUtils.startLockScreenWallpaperService(context);
        }
    }
}
