package com.github.runningforlife.photosniffer.app;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.avos.avoscloud.AVOSCloud;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.data.model.MyRealmMigration;
import com.github.runningforlife.photosniffer.data.remote.LeanCloudManager;
import com.github.runningforlife.photosniffer.service.LockScreenUpdateService;
import com.github.runningforlife.photosniffer.service.MyThreadFactory;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * the entry point for the application
 */

public class AppGlobals extends Application{
    private static final String TAG = "AppGlobal";

    private static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String PATH_NAME = "photos";
    private static final String PATH_CRASH_LOG = "log";
    private static final String LEAN_CLOUD_APP_ID = "Pivxf9C9FGTTHtyg7QXI1ICI-gzGzoHsz";
    private static final String LEAN_CLOUD_APP_KEY = "KCjSyXjVTA9mCIJVs7tDVkGS";
    private static AppGlobals sInstance;
    private static String sImagePath;
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

        sImagePath = ROOT_PATH + File.separator + appName + File.separator + PATH_NAME;

        initExceptionHandler();

        initLeanCloud();

        saveUserInfo();

        startLockScreenUpdater();
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
        if(TextUtils.isEmpty(sImagePath)){
            return getRootDir() + PATH_NAME;
        }
        return sImagePath;
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

    private File getLogFile(){
        // save to path
        String logPath = getRootDir() + PATH_CRASH_LOG;
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

    private void saveLog(Throwable t){
        File file = getLogFile();
        new Thread(new FileSaveRunnable(file, t))
                .start();
    }

    private class FileSaveRunnable implements Runnable{
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

    private String getRootDir(){
        String appName = getString(R.string.app_name);
        return ROOT_PATH + File.separator + appName + File.separator;
    }

    private void uploadLogToCloud(){
        String logPath = getRootDir() + PATH_CRASH_LOG;
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
        // delete file
        if(file.exists()){
            file.delete();
        }
    }

    private void saveUserInfo(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        String key = getString(R.string.pref_new_user);
        boolean isNewUser = SharedPrefUtil.getBoolean(key, true);
        if(isNewUser && ni.isConnected()){
            LeanCloudManager cloud = LeanCloudManager.getInstance();
            cloud.newUser(Build.FINGERPRINT);
            //SharedPrefUtil.putBoolean(key, false);
        }
    }

    // start lock screen updateAsync service
    private void startLockScreenUpdater(){
        Log.v(TAG,"startLockScreenUpdater()");
        if(Build.VERSION.SDK_INT >= 24){
            // start wallpaper service
            Intent intent1 = new Intent(this, LockScreenUpdateService.class);
            startService(intent1);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void waitForWifi(){
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
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo network = cm.getActiveNetworkInfo();
            if(network.isConnected() &&
                    network.getType() == ConnectivityManager.TYPE_WIFI){
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
