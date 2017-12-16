package com.github.runningforlife.photosniffer.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;

import com.github.runningforlife.photosniffer.ui.receiver.LockScreenWallpaperReceiver;
import com.github.runningforlife.photosniffer.utils.WallpaperUtils;


/**
 * Created by jason on 11/30/17.
 */

public class LockScreenUpdateService extends Service {
    private static final String TAG = "LockScreenUpdateService";

    public static final int EVENT_SET_LOCK_SCREEN_WALLPAPER = 1;

    private LockScreenWallpaperReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG,"onCreate()");
        HandlerThread ht = new HandlerThread("LockScreenWallpaper");
        ht.start();

        H handler = new H(ht.getLooper());
        //Screen on broadcast cannot be declared at manifest file
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        mReceiver = new LockScreenWallpaperReceiver(handler);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG,"onStartCommand(): startId = " + startId);
        // keep the service at the foreground
        startForeground(startId, new Notification());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG,"onDestroy()");
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class H extends  Handler {

        H(Looper looper){
            super(looper);
        }

        @TargetApi(24)
        @Override
        public void handleMessage(Message message) {
            Log.v(TAG,"handleMessage()");
            if (message.what == EVENT_SET_LOCK_SCREEN_WALLPAPER) {
                // set wallpaper from cache
                if (Build.VERSION.SDK_INT >= 24) {
                    WallpaperUtils.setWallpaperFromCache(getApplicationContext(), WallpaperManager.FLAG_LOCK);
                }
            }
        }
    }
}
