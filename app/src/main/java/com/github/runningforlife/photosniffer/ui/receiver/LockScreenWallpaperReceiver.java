package com.github.runningforlife.photosniffer.ui.receiver;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.app.AppGlobals;
import com.github.runningforlife.photosniffer.crawler.processor.ImageSource;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.service.LockScreenUpdateService;
import com.github.runningforlife.photosniffer.service.MyThreadFactory;
import com.github.runningforlife.photosniffer.utils.DisplayUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by jason on 11/30/17.
 */

public class LockScreenWallpaperReceiver extends BroadcastReceiver {
    private static final String TAG = "LockScreenReceiver";

    private Handler mHandler;

    public LockScreenWallpaperReceiver(Handler handler){
        mHandler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isAutoLockScreen = sp.getBoolean(context.getString(R.string.pref_enable_auto_lockscreen_wallpaper), true);

        if(Intent.ACTION_SCREEN_ON.equals(intent.getAction()) && isAutoLockScreen){
            Message message = mHandler.obtainMessage(LockScreenUpdateService.EVENT_SET_LOCK_SCREEN_WALLPAPER);
            message.sendToTarget();
        }
    }
}
