package com.github.runningforlife.photosniffer.app;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.avos.avoscloud.AVOSCloud;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.data.model.MyRealmMigration;
import com.github.runningforlife.photosniffer.data.remote.LeanCloudManager;
import com.github.runningforlife.photosniffer.service.LockScreenUpdateService;
import com.github.runningforlife.photosniffer.service.MyThreadFactory;
import com.github.runningforlife.photosniffer.service.WallpaperCacheService;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;
import com.github.runningforlife.photosniffer.utils.WallpaperUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * the entry point for the application
 */

public class AppGlobals extends Application {
    private static final String TAG = "AppGlobal";

    private static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String PATH_NAME = "photos";
    private static final String PATH_CRASH_LOG = "log";
    private static final String LEAN_CLOUD_APP_ID = "Pivxf9C9FGTTHtyg7QXI1ICI-gzGzoHsz";
    private static final String LEAN_CLOUD_APP_KEY = "KCjSyXjVTA9mCIJVs7tDVkGS";
    private static AppGlobals sInstance;
    // wifi state receiver
    private WifiStateReceiver mWifiStateReceiver;

    public static AppGlobals getInstance(){
        return sInstance;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        String appName = getString(R.string.app_name);

        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name(appName)
                .migration(new MyRealmMigration())
                .build();
        Realm.setDefaultConfiguration(realmConfig);
        // it seems that we should init here
        sInstance = AppGlobals.this;

        initExceptionHandler();

        initLeanCloud();

        saveUserInfo();

        if (Build.VERSION.SDK_INT >= 24) {
            WallpaperUtils.startLockScreenWallpaperService(getApplicationContext());
        }

        startUpdateWallpaperCache();
    }

    @Override
    public void onTrimMemory(int level){
        super.onTrimMemory(level);

        unRegisterWifiStateReceiver();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();

        unRegisterWifiStateReceiver();
    }

    public String getImagePath(){
        return MiscUtil.getRootDir() + File.separator + PATH_NAME;
    }

    private void initExceptionHandler(){
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

    private void initLeanCloud(){
        AVOSCloud.initialize(this, LEAN_CLOUD_APP_ID, LEAN_CLOUD_APP_KEY);
    }

    private String buildLogFileName(){
        return "log_" + System.currentTimeMillis() + ".txt";
    }

    private File getLogFile() {
        // save to path
        String logPath = MiscUtil.getRootDir() + File.separator + PATH_CRASH_LOG;
        File path = new File(logPath);
        if(!path.exists()){
            path.mkdirs();
        }

        File file = new File(path,buildLogFileName());
        if(!file.exists()){
            try {
                file.createNewFile();
                return file;
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (SecurityException se){
                se.printStackTrace();
            }
        }

        return file;
    }

    private void saveLog(Throwable t) {
        File file = getLogFile();
        new Thread(new FileSaveRunnable(file, t))
                .start();
    }

    private class FileSaveRunnable implements Runnable {
        private File file;
        private Throwable throwable;

        FileSaveRunnable(File file,Throwable t){
            this.file = file;
            this.throwable = t;
        }

        @Override
        public void run() {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                PrintWriter pw = new PrintWriter(fos);

                DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.US);
                pw.println("LOG Date: " + df.format(new Date()));
                pw.println("Device FingerPrint: " + Build.FINGERPRINT);
                pw.println();
                throwable.printStackTrace(pw);

                pw.flush();
                fos.flush();

                pw.close();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // save to cloud if wifi available
            if (MiscUtil.isWifiConnected(getApplicationContext())) {
                saveLogToCloud(file);
            }else{
                // waiting for wifi
                waitForWifi();
            }
        }
    }

    private void uploadLogToCloud() {
        String logPath = MiscUtil.getRootDir() + File.separator + PATH_CRASH_LOG;
        File file = new File(logPath);
        if(file.exists()){
            File[] logs = file.listFiles();
            for(File log : logs){
                if(log.isFile()){
                    saveLogToCloud(log);
                }
            }
        }
    }

    private void saveLogToCloud(File file){
        if(file.length() <= 0) return;

        LeanCloudManager cloudManager = LeanCloudManager.getInstance();
        cloudManager.saveFile(file);
    }

    private void saveUserInfo() {
        if(isNewUser() && MiscUtil.isConnected(getApplicationContext())){
            LeanCloudManager cloud = LeanCloudManager.getInstance();
            cloud.newUser(Build.FINGERPRINT);
        }
    }

    private boolean isNewUser() {
        String key = getString(R.string.pref_new_user);
        return SharedPrefUtil.getBoolean(key, true);
    }

    private void startUpdateWallpaperCache() {
        if (isNewUser()) {
            startWallpaperCacheUpdaterAlarm();

        } else {
            if(Build.VERSION.SDK_INT >= 24) {
                int jobId = MiscUtil.getJobId();
                JobScheduler js = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                JobInfo jobInfo = js.getPendingJob(jobId);
                if (jobInfo == null) {
                    startWallpaperUpdaterJob(jobId);
                }
            }
        }
    }

    @TargetApi(21)
    private void startWallpaperUpdaterJob(int jobId) {
        int interval = Integer.parseInt(getString(R.string.wallpaper_cache_update_interval));

        ComponentName jobService = new ComponentName(getApplicationContext(), WallpaperCacheService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, jobService);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(interval)
                .setMinimumLatency(1000)
                .setPersisted(true);

        JobScheduler js = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        //js.cancel(jobId);
        js.schedule(builder.build());
    }

    private void startWallpaperCacheUpdaterAlarm() {
        String action = "com.github.runningforlife.UPATE_WALLPAPER_CACHE";
        Intent intent = new Intent(action);
        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        int interval = 100;
        if (Build.VERSION.SDK_INT >= 21) {
            am.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, pi);
        } else {
            am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10, pi);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void waitForWifi() {
        Log.v(TAG,"waitForWifi()");
        registerWifiStateReceiver();
    }

    private void registerWifiStateReceiver(){
        mWifiStateReceiver = new WifiStateReceiver();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(mWifiStateReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unRegisterWifiStateReceiver(){
        if(mWifiStateReceiver != null){
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            lbm.unregisterReceiver(mWifiStateReceiver);
        }
    }

    private class WifiStateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(MiscUtil.isWifiConnected(context)){
                MyThreadFactory.getInstance().
                        newThread(new Runnable() {
                    @Override
                    public void run() {
                        uploadLogToCloud();
                    }
                }).start();
            }
        }
    }


    private class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler{

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if(e != null) {
                saveLog(e);
            }
        }
    }
}
