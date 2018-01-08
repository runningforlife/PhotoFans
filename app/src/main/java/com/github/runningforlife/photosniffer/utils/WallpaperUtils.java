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
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.service.LockScreenUpdateService;
import com.github.runningforlife.photosniffer.service.WallpaperJobService;
import com.github.runningforlife.photosniffer.service.WallpaperUpdaterService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static com.github.runningforlife.photosniffer.utils.MiscUtil.JOB_NIGHT_TIME;

/**
 * a utility class related with wallpaper
 */

public class WallpaperUtils {
    private static final String TAG = "WallpaperUtils";
    private static final SimpleDateFormat sDf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US);

    public static final String ALARM_AUTO_WALLPAPER = "com.github.runningforlife.AUTO_WALLPAPER";

    public static void startWallpaperCacheUpdaterService(Context context) {
        Realm rm = Realm.getDefaultInstance();
        try {
            RealmQuery<ImageRealm> query = rm.where(ImageRealm.class);
            RealmResults<ImageRealm> wallpaper = query
                    .equalTo("mIsFavor", true)
                    .or()
                    .equalTo("mIsUsed", false)
                    .findAll();

            if (wallpaper.size() == 0) {
                wallpaper = query
                        .or()
                        .equalTo("mIsUsed", true)
                        .findAll();
            }
            // 10 image cached a time
            List<String> wallpapers = new ArrayList<>(15);
            for (int i = 0; i < 15 && i < wallpaper.size(); ++i) {
                wallpapers.add(wallpaper.get(i).getUrl());
            }

            Intent intent = new Intent(context, WallpaperUpdaterService.class);
            intent.putStringArrayListExtra("wallpapers", (ArrayList<String>) wallpapers);
            context.startService(intent);
        } finally {
            rm.close();
        }
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
        JobScheduler js = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        js.cancel(jobId);

        String keyWallpaperInterval = context.getString(R.string.pref_auto_wallpaper_interval);
        int interval = Integer.parseInt(SharedPrefUtil.getString(keyWallpaperInterval, "900000"));

        ComponentName jobService = new ComponentName(context, WallpaperJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, jobService);
        builder.setPeriodic(interval)
                .setPersisted(true);

        js.schedule(builder.build());
    }

    @TargetApi(21)
    public static void cancelSchedulerJob(Context context, int jobId) {
        JobScheduler js = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        js.cancel(jobId);
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

    public static void cancelAutoWallpaperAlarm(Context context) {
        Log.v(TAG,"cancelAutoWallpaperAlarm()");
        PendingIntent alarmIntent = MiscUtil.getPendingIntent(ALARM_AUTO_WALLPAPER, context);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(alarmIntent);
    }

    public static void startLockScreenWallpaperService(Context context) {
        // start wallpaper service
        Intent intent = new Intent(context, LockScreenUpdateService.class);
        context.startService(intent);
    }

    public static void stopLockScreenWallpaperService(Context context) {
        Intent intent = new Intent(context, LockScreenUpdateService.class);
        context.stopService(intent);
    }

    public static void setWallpaperFromCache(final Context context, final int flag) {
        Log.v(TAG, "setWallpaperFromCache()");
        new Thread(new Runnable() {
            @Override
            public void run() {
                WallpaperUtils.startWallpaperFromCache(context, flag);
            }
        }).start();

    }

    public static void restartAutoWallpaperForNightTime(Context context) {
        String prefNightTime = context.getString(R.string.pref_night_time_interval);
        String prefNightTimeStarting = context.getString(R.string.pref_night_time_starting);
        long nightTimeInterval = SharedPrefUtil.getLong(prefNightTime, 0);
        long nightTimeStart = SharedPrefUtil.getLong(prefNightTimeStarting, 0);
        if (nightTimeInterval > 0 && nightTimeStart > 0) {
            cancelAutoWallpaperAlarm(context);
            cancelSchedulerJob(context, MiscUtil.getJobId(MiscUtil.JOB_WALLPAPER_SET));

            startNightTimeJobService(context);
            // ok change starting time to the next day
            SharedPrefUtil.putLong(prefNightTimeStarting, System.currentTimeMillis() +
                    TimeUnit.DAYS.toMillis(1));
        }

    }

    @TargetApi(21)
    private static void startNightTimeJobService(Context context) {
        JobScheduler js = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);

        String prefNightTimeDuration = context.getString(R.string.pref_night_time_interval);
        long nightTimeDuration = SharedPrefUtil.getLong(prefNightTimeDuration, 0);
        if (nightTimeDuration > 0) {
            ComponentName jobService = new ComponentName(context, WallpaperJobService.class);
            JobInfo.Builder builder = new JobInfo.Builder(MiscUtil.getJobId(JOB_NIGHT_TIME), jobService);
            JobInfo jobInfo = builder.setPersisted(true)
                   //.setPeriodic(nightTimeDuration)
                   .setMinimumLatency(nightTimeDuration + 30*1000)
                   .build();
            if (js != null) {
                js.schedule(jobInfo);
            }
        }
    }

    private static void startWallpaperFromCache(Context context, int flag) {
        RealmApi realmApi = RealmApiImpl.getInstance();
        HashMap<String, String> params = new HashMap<>();
        params.put("mIsUsed", Boolean.toString(true));
        params.put("mIsFavor", Boolean.toString(false));
        params.put("mIsWallpaper", Boolean.toString(Boolean.TRUE));
        RealmResults<ImageRealm> wallpapers = (RealmResults<ImageRealm>) realmApi.querySync(ImageRealm.class, params);

        if (wallpapers.size() == 0) {
            // try to cache images
            realmApi.closeRealm();
            startWallpaperCacheUpdaterService(context);
            return;
        }

        String keyCacheIdx = context.getString(R.string.pref_wallpaper_cache_index);
        int cacheIdx = SharedPrefUtil.getInt(keyCacheIdx, 0);
        // find a wallpaper
        cacheIdx %= wallpapers.size();

        WallpaperManager wm = WallpaperManager.getInstance(context);
        String url = wallpapers.get(cacheIdx).getUrl();
        try {
            FutureTarget<Bitmap> futureTarget =
                    Glide.with(context)
                            .load(url)
                            .asBitmap()
                            .centerCrop()
                            .into(Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT);
            Bitmap bitmap = futureTarget.get(5000, TimeUnit.MILLISECONDS);
            if (Build.VERSION.SDK_INT >= 24) {
                wm.setBitmap(bitmap, null, false, flag);
            } else {
                wm.setBitmap(bitmap);
            }
            // recording wallpaper setting
            recordWallpaperSetting(url, null);
        } catch (Exception e) {
            Log.e(TAG,"fail to set wallpaper");
            recordWallpaperSetting(url, e);
        } finally {
            realmApi.closeRealm();
            // save current idx
            SharedPrefUtil.putInt(keyCacheIdx, ++cacheIdx);
        }
    }

    private static void recordWallpaperSetting(String url, Exception ex) {
        File logDir = new File(MiscUtil.getLogDir(), MiscUtil.getWallpaperLog());
        if (!logDir.exists()) {
            try {
                if(!logDir.createNewFile()) {
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(logDir, true);
            PrintWriter pw = new PrintWriter(fos);

            pw.println("wallpaper setting date:" + sDf.format(new Date()));
            pw.println("wallpaper url:" + url);
            if (ex != null) {
                pw.println("fail to set wallpaper" + ex);
            }
            pw.println();

            pw.flush();

            pw.close();
            fos.close();
        } catch (IOException e) {

        }
    }
}
