package com.github.runningforlife.photosniffer.utils;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.app.AppGlobals;
import com.github.runningforlife.photosniffer.data.remote.LeanCloudManager;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by jason on 6/5/17.
 */

public class MiscUtil {
    public static final String JOB_WALLPAPER_CACHE = "cache";
    public static final String JOB_WALLPAPER_SET = "setting";
    public static final String JOB_NIGHT_TIME = "night_time";

    private static final String APP_NAME = "PhotoSniffer";
    private static final String PATH_WALLPAPER_CACHE = "wallpapers";
    private static final String PATH_CRASH_LOG = "log";
    private static final String PATH_PHOTOS = "photos";
    private static final String PATH_GLIDE_CACHE = "cache";
    private static final String WALLPAPER_LOG = "wallpaper_setting.log";

    public static OkHttpClient buildOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(10000, TimeUnit.MILLISECONDS)
                .connectTimeout(10000, TimeUnit.MILLISECONDS);

        return builder.build();
    }

    public static int getJobId(@NonNull String jn) {
        return jn.hashCode();
    }

    public static String getPhotoDir() {
        return getRootDir() + File.separator + PATH_PHOTOS;
    }

    public static String getWallpaperCacheDir() {
        return getRootDir() + File.separator + PATH_WALLPAPER_CACHE;
    }

    public static String getWallpaperLog() {
        return WALLPAPER_LOG;
    }

    public static String getGlideCacheDir() {
        File cacheDir = AppGlobals.getInstance().getExternalCacheDir();
        if (cacheDir == null || !cacheDir.exists()) {
            cacheDir = AppGlobals.getInstance().getCacheDir();
        }
        File file = new File(cacheDir, PATH_GLIDE_CACHE);
        return file.getAbsolutePath();
    }

    public static String getLogDir() {
        return getRootDir() + File.separator + PATH_CRASH_LOG;
    }

    public static String getRootDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + APP_NAME;
    }

    public static PendingIntent getPendingIntent(String action, Context context) {
        Intent intent = new Intent(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni != null && ni.isConnected();
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni != null && ni.isConnected() && ni.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isMobileConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni != null && ni.isConnected() && ni.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public static void saveLogToCloud(File file) {
        if(file.length() <= 0) return;

        LeanCloudManager cloudManager = LeanCloudManager.getInstance();
        cloudManager.saveFile(file);
    }
}
