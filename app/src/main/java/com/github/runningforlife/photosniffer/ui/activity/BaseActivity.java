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

    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}
