package com.github.runningforlife.photosniffer.service;

import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;
import com.github.runningforlife.photosniffer.utils.WallpaperUtils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * a service to interact with JobScheduler
 */

@TargetApi(21)
public class WallpaperJobService extends JobService {
    private static final String TAG = "WallpaperJobService";

    @Override
    public void onCreate() {
        super.onCreate();
        // make sure that lock screen wallpaper service is active
        WallpaperUtils.startLockScreenWallpaperService(getApplicationContext());
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.v(TAG,"onStartJob()");
        int jobId = params.getJobId();
        if (jobId == MiscUtil.getJobId(MiscUtil.JOB_WALLPAPER_CACHE)) {
            String prefWifi = getString(R.string.pref_wifi_download);
            boolean isWifiOnly = SharedPrefUtil.getBoolean(prefWifi, true);
            if ((isWifiOnly && MiscUtil.isWifiConnected(getApplicationContext())) || (MiscUtil.isConnected(getApplicationContext()))) {
                WallpaperUtils.startWallpaperCacheUpdaterService(getApplicationContext());
            }
        } else if (jobId == MiscUtil.getJobId(MiscUtil.JOB_WALLPAPER_SET)) {
            WallpaperUtils.setWallpaperFromCache(getApplicationContext(), WallpaperManager.FLAG_SYSTEM);
        }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}
