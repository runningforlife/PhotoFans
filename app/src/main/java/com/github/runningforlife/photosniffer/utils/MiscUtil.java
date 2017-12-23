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

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by jason on 6/5/17.
 */

public class MiscUtil {
    public static final String JOB_WALLPAPER_CACHE = "cache";
    public static final String JOB_WALLPAPER_SET = "setting";

    private static final String APP_NAME = "PhotoSniffer";
    private static final String PATH_WALLPAPER_CACHE = "wallpapers";
    private static final String PATH_CRASH_LOG = "log";
    private static final String PATH_PHOTOS = "photos";
    private static final String PATH_GLIDE_CACHE = "cache";

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

    public static String getGlideCacheDir() {
        return APP_NAME + File.separator + PATH_GLIDE_CACHE;
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

    public static void preloadImage(ImageView iv) {
        Resources res = iv.getContext().getResources();
        Drawable d = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            d = res.getDrawable(R.drawable.ic_photo_grey_24dp, null);
        }else{
            d = res.getDrawable(R.drawable.ic_photo_grey_24dp);
        }
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iv.setImageDrawable(d);
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
}
