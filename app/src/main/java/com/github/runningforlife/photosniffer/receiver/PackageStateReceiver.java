package com.github.runningforlife.photosniffer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.runningforlife.photosniffer.utils.MiscUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * listen package state
 */

public class PackageStateReceiver extends BroadcastReceiver {
    private static final String TAG = "PackageStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.v(TAG,"onReceive(): action = " + action);
        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            removeAppDir();
        }
    }

    private void removeAppDir() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileUtils.deleteDirectory(new File(MiscUtil.getRootDir()));
                } catch (IOException e) {
                    Log.v(TAG,"fail to remove root directory");
                }
            }
        }).start();
    }
}
