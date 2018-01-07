package com.github.runningforlife.photosniffer.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.File;

/**
 * a common used activity class
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    static final String PATH_NAME = "photos";

    static final String FRAGMENT_TAG_FAVOR = "favorite";
    static final String FRAGMENT_TAG_WALLPAPER = "wallpaper";
    static final String FRAGMENT_TAG_GALLERY = "gallery";

    private BroadcastReceiver mWifiStateReceiver;

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);

        if (!MiscUtil.isWifiConnected(this)) {
            registerWifiStateReceiver();
        } else {
            uploadLogToCloud();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unRegisterWifiStateReceiver();
    }

    private void registerWifiStateReceiver() {
        mWifiStateReceiver = new WifiStateReceiver();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(mWifiStateReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unRegisterWifiStateReceiver() {
        if (mWifiStateReceiver != null) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            lbm.unregisterReceiver(mWifiStateReceiver);
        }
    }

    private class WifiStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MiscUtil.isWifiConnected(context)) {
                uploadLogToCloud();
            }
        }
    }

    private void uploadLogToCloud() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String logPath = MiscUtil.getLogDir();
                File file = new File(logPath);
                if (file.exists()) {
                    File[] logs = file.listFiles();
                    for (File log : logs) {
                        if (log.isFile()) {
                            MiscUtil.saveLogToCloud(log);
                        }
                    }
                }
            }
        }).start();
    }
}
