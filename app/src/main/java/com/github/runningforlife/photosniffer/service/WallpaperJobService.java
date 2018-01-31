package com.github.runningforlife.photosniffer.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.WallpaperManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.receiver.LockScreenWallpaperReceiver;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;
import com.github.runningforlife.photosniffer.utils.WallpaperUtils;

import static com.github.runningforlife.photosniffer.utils.MiscUtil.JOB_NIGHT_TIME;
import static com.github.runningforlife.photosniffer.utils.MiscUtil.JOB_WALLPAPER_CACHE;
import static com.github.runningforlife.photosniffer.utils.MiscUtil.JOB_WALLPAPER_SET;

/**
 * a service to interact with JobScheduler
 */

@TargetApi(21)
public class WallpaperJobService extends JobService {
    private static final String TAG = "WallpaperJobService";

    public static final int EVENT_SET_LOCK_SCREEN_WALLPAPER = 1;

    LockScreenWallpaperReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        // start a new thread instead of use main thread
        HandlerThread ht = new HandlerThread("WallpaperJob");
        ht.start();

        H handler = new H(ht.getLooper());
        //Screen on broadcast cannot be declared at manifest file
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        mReceiver = new LockScreenWallpaperReceiver(handler);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG,"onStartCommand(): startId = " + startId);
        // keep the service at the foreground
        startForeground(startId, new Notification());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG,"onDestroy()");
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.v(TAG,"onStartJob()");
        int jobId = params.getJobId();
        if (jobId == MiscUtil.getJobId(JOB_WALLPAPER_CACHE)) {
            String prefWifi = getString(R.string.pref_wifi_download);
            boolean isWifiOnly = SharedPrefUtil.getBoolean(prefWifi, true);
            if ((isWifiOnly && MiscUtil.isWifiConnected(getApplicationContext())) ||
                    (!isWifiOnly && MiscUtil.isConnected(getApplicationContext()))) {
                WallpaperUtils.startWallpaperCacheUpdaterService(getApplicationContext());
            }
        }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private final class H extends Handler {

        H(Looper looper){
            super(looper);
        }

        @TargetApi(24)
        @Override
        public void handleMessage(Message message) {
            Log.v(TAG,"handleMessage()");
            if (message.what == EVENT_SET_LOCK_SCREEN_WALLPAPER) {
                // set wallpaper from cache
                String prefLockWallpaper = getString(R.string.pref_enable_auto_lockscreen_wallpaper);
                boolean isLockWallpaperEnabled = SharedPrefUtil.getBoolean(prefLockWallpaper, false);
                if (Build.VERSION.SDK_INT >= 24 && isLockWallpaperEnabled) {
                    WallpaperUtils.startWallpaperFromCache(getApplicationContext(), WallpaperManager.FLAG_LOCK);
                }

                String prefAutoWallpaper = getString(R.string.pref_automatic_wallpaper);
                boolean isAutoWallpaperEnabled = SharedPrefUtil.getBoolean(prefAutoWallpaper, true);
                if (isAutoWallpaperEnabled) {
                    WallpaperUtils.startWallpaperFromCache(getApplicationContext(), WallpaperManager.FLAG_SYSTEM);
                }
            }
        }
    }

}
