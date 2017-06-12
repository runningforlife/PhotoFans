package com.github.runningforlife.photosniffer.app;

import android.app.Application;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.avos.avoscloud.AVOSCloud;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.model.ImageRealmMigration;
import com.github.runningforlife.photosniffer.remote.LeanCloudManager;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * the entry point for the application
 */

public class AppGlobals extends Application{

    private static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String PATH_NAME = "photos";
    private static final String PATH_CRASH_LOG = "log";
    private static final String LEAN_CLOUD_APP_ID = "Pivxf9C9FGTTHtyg7QXI1ICI-gzGzoHsz";
    private static final String LEAN_CLOUD_APP_KEY = "KCjSyXjVTA9mCIJVs7tDVkGS";
    private static AppGlobals sInstance;
    private static String sImagePath;

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
                .migration(new ImageRealmMigration())
                .build();
        Realm.setDefaultConfiguration(realmConfig);
        // it seems that we should init here
        sInstance = AppGlobals.this;

        sImagePath = ROOT_PATH + File.separator + appName + File.separator + PATH_NAME;
        File file = new File(sImagePath);
        if(!file.exists()) {
            file.mkdirs();
        }

        initExceptionHandler();

        initLeanCloud();
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

    private class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler{

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if(MiscUtil.isWifiConnected(getApplicationContext())){
                saveLogToCloud(e);
            }else{
                new Thread(new FileSaveRunnable(getLogFile(),e))
                        .start();
            }
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
                }
            }

            return file;
        }
    }

    private void initLeanCloud(){
        AVOSCloud.initialize(this, LEAN_CLOUD_APP_ID, LEAN_CLOUD_APP_KEY);
    }

    private String buildLogFileName(){
        return "log_" + System.currentTimeMillis() + ".txt";
    }

    private void saveLogToCloud(Throwable e){
        LeanCloudManager cloud = LeanCloudManager.getInstance();
        String fileName = buildLogFileName();
        StringBuilder data = new StringBuilder();
        data.append("date: " + new Date());
        data.append("\n");
        data.append("device fingerprint: " + Build.FINGERPRINT);
        data.append("\n");
        data.append(e.toString());

        cloud.saveFile(fileName, data.toString());
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

                DateFormat df = DateFormat.getDateInstance();
                pw.println("LOG Date: " + df.format(new Date()));
                pw.println("Device FingerPrint: " + Build.FINGERPRINT);
                pw.println();
                pw.print(throwable.toString());

                pw.flush();
                fos.flush();

                pw.close();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getRootDir(){
        String appName = getString(R.string.app_name);
        return ROOT_PATH + File.separator + appName + File.separator;
    }
}
