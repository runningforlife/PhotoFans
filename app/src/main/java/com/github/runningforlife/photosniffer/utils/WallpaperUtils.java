package com.github.runningforlife.photosniffer.utils;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.service.LockScreenUpdateService;
import com.github.runningforlife.photosniffer.service.WallpaperJobService;
import com.github.runningforlife.photosniffer.service.WallpaperUpdaterService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.JOB_SCHEDULER_SERVICE;

/**
 * a utility class related with wallpaper
 */

public class WallpaperUtils {
    private static final String TAG = "WallpaperUtils";
    public static final String ALARM_AUTO_WALLPAPER = "com.github.runningforlife.AUTO_WALLPAPER";

    public static void startWallpaperCacheUpdaterService(Context context) {
        List<String> wallpapers = new ArrayList<>();
        Realm rm = Realm.getDefaultInstance();
        RealmQuery<ImageRealm> query = rm.where(ImageRealm.class);
        RealmResults<ImageRealm> wallpaper = query
                .equalTo("mIsWallpaper", true)
                .or()
                .equalTo("mIsFavor", true)
                .equalTo("mIsCached", false)
                .findAll();

        if (wallpaper.size() == 0) {
            wallpaper = query
                    .or()
                    .equalTo("mIsUsed", true)
                    .equalTo("mIsCached", false)
                    .findAll();
        }
        // 10 image cached a time
        for (int i = 0; i < 15 && i < wallpaper.size(); ++i) {
            wallpapers.add(wallpaper.get(i).getUrl());
        }

        Intent intent = new Intent(context, WallpaperUpdaterService.class);
        intent.putStringArrayListExtra("wallpapers", (ArrayList<String>) wallpapers);
        context.startService(intent);
    }

    public static void startWallpaperCacheUpdaterAlarm(Context context) {
        String action = "com.github.runningforlife.UPATE_WALLPAPER_CACHE";
        PendingIntent pi = MiscUtil.getPendingIntent(action, context);
        if (pi != null) {
            pi.cancel();
        }
        AlarmManager am = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        int interval = Integer.parseInt(context.getString(R.string.wallpaper_cache_update_interval));
        if (Build.VERSION.SDK_INT >= 21) {
            am.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, pi);
        } else {
            am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10, pi);
        }
    }

    @TargetApi(21)
    public static void startWallpaperSettingJob(Context context, int jobId) {
        String keyWallpaperInterval = context.getString(R.string.pref_auto_wallpaper_interval);
        int interval = Integer.parseInt(SharedPrefUtil.getString(keyWallpaperInterval, "900000"));

        ComponentName jobService = new ComponentName(context, WallpaperJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, jobService);
        builder.setPeriodic(interval)
                .setPersisted(true);

        JobScheduler js = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        js.cancel(jobId);
        js.schedule(builder.build());
    }

    @TargetApi(21)
    public static void startWallpaperUpdaterJob(Context context, int jobId) {
        int interval = Integer.parseInt(context.getString(R.string.wallpaper_cache_update_interval));

        ComponentName jobService = new ComponentName(context, WallpaperJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, jobService);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(interval)
                .setPersisted(true);

        JobScheduler js = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        // first we cancel current scheduled job
        js.cancel(jobId);
        js.schedule(builder.build());
    }

    public static void startAutoWallpaperAlarm(Context context) {
        PendingIntent pi = MiscUtil.getPendingIntent(ALARM_AUTO_WALLPAPER, context);

        String keyAutoWallpaper = context.getString(R.string.pref_auto_wallpaper_interval);
        int alarmInterval = Integer.parseInt(SharedPrefUtil.getString(keyAutoWallpaper, "900000"));
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + alarmInterval, pi);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + alarmInterval, pi);
        }
    }

    public static void startLockScreenWallpaperService(Context context) {
        // start wallpaper service
        Intent intent = new Intent(context, LockScreenUpdateService.class);
        context.startService(intent);
    }

    public static void setWallpaperFromCache(Context context, int flag) {
        Log.v(TAG, "setWallpaperFromCache()");
        String wallpaperDir = MiscUtil.getWallpaperCacheDir();
        File file = new File(wallpaperDir);
        if (file.exists()) {
            File[] wallpapers = file.listFiles();
            if (wallpapers.length == 0) {
                // try to cache images
                startWallpaperCacheUpdaterService(context);
                return;
            }

            String keyCacheIdx = context.getString(R.string.pref_wallpaper_cache_index);
            int cacheIdx = SharedPrefUtil.getInt(keyCacheIdx, 0)%wallpapers.length;
            // find a wallpaper
            while(!wallpapers[cacheIdx].exists()) {
                ++cacheIdx;
                cacheIdx %= wallpapers.length;
            }

            WallpaperManager wm = WallpaperManager.getInstance(context);
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = true;
                Bitmap bitmap = BitmapFactory.decodeFile(wallpapers[cacheIdx].getAbsolutePath(), options);
                if (Build.VERSION.SDK_INT >= 24) {
                    wm.setBitmap(bitmap, null, false, flag);
                } else {
                    wm.setBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // save current idx
            SharedPrefUtil.putInt(keyCacheIdx, ++cacheIdx);
        }
    }
}
