package com.github.runningforlife.photosniffer.service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.github.runningforlife.photosniffer.app.AppGlobals;
import com.github.runningforlife.photosniffer.crawler.processor.ImageSource;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.ui.receiver.LockScreenWallpaperReceiver;
import com.github.runningforlife.photosniffer.utils.BitmapUtil;
import com.github.runningforlife.photosniffer.utils.DisplayUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by jason on 11/30/17.
 */

public class LockScreenUpdateService extends Service {
    private static final String TAG = "LockScreenUpdateService";

    public static final int EVENT_SET_LOCK_SCREEN_WALLPAPER = 1;

    private static AtomicInteger sWallpaperCount = new AtomicInteger(0);

    private LockScreenWallpaperReceiver mReceiver;

    private static final DisplayMetrics dm = DisplayUtil.getScreenDimen();


    @Override
    public void onCreate(){
        super.onCreate();

        HandlerThread ht = new HandlerThread("LockScreenWallpaper");
        ht.start();

        H handler = new H(ht.getLooper());

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);

        mReceiver = new LockScreenWallpaperReceiver(handler);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.v(TAG,"onStartCommand(): startId = " + startId);

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @TargetApi(24)
    private void setWallpaper(){

        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<ImageRealm> wallpaper = realm.where(ImageRealm.class)
                    .equalTo("mIsWallpaper", true)
                    .or()
                    .equalTo("mIsFavor", true)
                    .or()
                    .equalTo("mIsUsed", true)
                    .findAll();
            if (wallpaper.size() <= 0) return;

            Log.v(TAG, "setWallpaper()");
            final int pos = sWallpaperCount.getAndIncrement()%wallpaper.size();
            String imgUrl = wallpaper.get(pos).getUrl();

            cacheNextWallpaper(imgUrl, false);


        }finally {
            realm.close();
        }
    }

    private void setScreenLockWallpaper(Bitmap bm, int flag){
        Log.v(TAG,"setScreenLockWallpaper()");
        // check bitmap size?
        WallpaperManager wpm = WallpaperManager.getInstance(getApplicationContext());
        try {
            if (Build.VERSION.SDK_INT >= 24 && flag != -1) {
                wpm.setBitmap(bm, null, true, flag);
            } else {
                wpm.setBitmap(bm);
            }
        } catch (IOException e) {
            //mView.onWallpaperSetDone(false);
            e.printStackTrace();
        }
    }

    // it'd better to cache some images previously
    // 1. network may have problems, so we need cache
    // 2. use cache images to speed up
    private void cacheNextWallpaper(String imgUrl, final boolean isFirstTime){
        Log.v(TAG,"cacheNextWallpaper()");
        GlideLoaderListener listener = new GlideLoaderListener(null);
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.d(TAG, "onImageLoadDone()");
                if (o instanceof Bitmap) {
                    setScreenLockWallpaper((Bitmap)o, WallpaperManager.FLAG_LOCK);
                }
            }
        });

        GlideLoader.downloadOnly(getApplicationContext(), imgUrl, listener, Priority.IMMEDIATE,
                dm.widthPixels, dm.heightPixels, true);
    }

    private final class H extends  Handler{

        H(Looper looper){
            super(looper);
        }

        @TargetApi(24)
        @Override
        public void handleMessage(Message message){
            if (message.what == EVENT_SET_LOCK_SCREEN_WALLPAPER) {
                setWallpaper();
            }
        }
    }
}
