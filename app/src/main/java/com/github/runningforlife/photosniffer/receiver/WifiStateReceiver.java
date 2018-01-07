package com.github.runningforlife.photosniffer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.data.remote.LeanCloudManager;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;
import com.github.runningforlife.photosniffer.utils.WallpaperUtils;

import java.io.File;

/**
 * this is for API < 21(Android L); for Android LL, use JobScheduler
 */

public class WifiStateReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiStateReceiver()";

    private static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String PATH_CRASH_LOG = "log";

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            if (MiscUtil.isWifiConnected(context)) {
                new Thread(new Runnable() {
                            @Override
                            public void run() {
                                uploadLogToCloud();
                            }
                        }).start();
            }

            String wifiOnlyMode = context.getString(R.string.pref_wifi_download);
            boolean isWifiOnly = SharedPrefUtil.getBoolean(wifiOnlyMode, true);
            // pre-fill image cache
            if ((isWifiOnly && MiscUtil.isWifiConnected(context)) ||
                (!isWifiOnly && MiscUtil.isConnected(context))) {

                if (Build.VERSION.SDK_INT >= 21) {
                    WallpaperUtils.startWallpaperUpdaterJob(context, MiscUtil.getJobId(MiscUtil.JOB_WALLPAPER_CACHE));
                } else {
                    WallpaperUtils.startWallpaperCacheUpdaterAlarm(context);
                }
            }
        }
    }

    private void uploadLogToCloud() {
        String logPath = MiscUtil.getLogDir();
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
        // deleteSync file
        if(file.exists()){
            file.delete();
        }
    }
}
