package com.github.runningforlife.photosniffer.service;

import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.os.HandlerThread;
import android.util.Log;

import com.github.runningforlife.photosniffer.R;
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

    @Override
    public void onCreate() {
        super.onCreate();
        // start a new thread instead of use main thread
        HandlerThread ht = new HandlerThread("WallpaperJob");
        ht.start();
        if (Build.VERSION.SDK_INT >= 24) {
            // make sure that lock screen wallpaper service is active
            WallpaperUtils.startLockScreenWallpaperService(getApplicationContext());
        }
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.v(TAG,"onStartJob()");
        String prefNightSetting = getString(R.string.pref_night_time_setting);
        boolean isEnabled = SharedPrefUtil.getBoolean(prefNightSetting, false);
        if (isEnabled && !MiscUtil.isSleepTime(getApplicationContext())) {
            int jobId = params.getJobId();
            if (jobId == MiscUtil.getJobId(JOB_WALLPAPER_CACHE)) {
                String prefWifi = getString(R.string.pref_wifi_download);
                boolean isWifiOnly = SharedPrefUtil.getBoolean(prefWifi, true);
                if ((isWifiOnly && MiscUtil.isWifiConnected(getApplicationContext())) ||
                        (!isWifiOnly && MiscUtil.isConnected(getApplicationContext()))) {
                    WallpaperUtils.startWallpaperCacheUpdaterService(getApplicationContext());
                }
            } else if (jobId == MiscUtil.getJobId(JOB_WALLPAPER_SET)) {
                WallpaperUtils.setWallpaperFromCache(getApplicationContext(), WallpaperManager.FLAG_SYSTEM);
            // night time end, restart normal auto wallpaper
            } else if (jobId == MiscUtil.getJobId(JOB_NIGHT_TIME)) {
                WallpaperUtils.cancelSchedulerJob(getApplicationContext(), MiscUtil.getJobId(JOB_NIGHT_TIME));
                WallpaperUtils.startWallpaperSettingJob(getApplicationContext(), MiscUtil.getJobId(JOB_WALLPAPER_SET));
                WallpaperUtils.startWallpaperCacheUpdaterService(getApplicationContext());
            }
        } else {
            WallpaperUtils.restartAutoWallpaperForNightTime(getApplicationContext());
        }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}
