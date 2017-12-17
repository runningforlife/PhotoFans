package com.github.runningforlife.photosniffer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.runningforlife.photosniffer.data.cache.DiskWallpaperCache;
import com.github.runningforlife.photosniffer.data.cache.WallpaperCacheRunnable;
import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * fetch images from web and cache them
 */

public class WallpaperUpdaterService extends Service {
    private static final String TAG = "WallpaperUpdaterService";

    private DiskWallpaperCache mDiskCache;
    private Executor mUpdateExecutor;
    private RealmApi mRealmApi;
    private volatile ServiceHandler mHandler;
    private volatile Looper mLooper;


    private  final class ServiceHandler extends Handler {

        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            onHandleIntent((Intent)message.obj);
        }
    }

    public WallpaperUpdaterService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();

        mLooper = handlerThread.getLooper();
        mHandler = new ServiceHandler(mLooper);

        mDiskCache = new DiskWallpaperCache(new File(MiscUtil.getWallpaperCacheDir()));

        mUpdateExecutor = Executors.newFixedThreadPool(2);

        mRealmApi = RealmApiImpl.getInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mLooper.quit();

        mRealmApi.decRef();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message message = mHandler.obtainMessage();
        message.obj = intent;
        message.sendToTarget();
        return START_REDELIVER_INTENT;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onHandleIntent(Intent intent) {
        Log.v(TAG,"onHandleIntent()");
        // every time we try to download & cache 10 images
        List<String> wallpapers = intent.getStringArrayListExtra("wallpapers");
        if (wallpapers != null && wallpapers.size() > 0) {
            try {
                downloadAndCacheWallpapers(wallpapers);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // we are done
        stopSelf();
    }

    private void downloadAndCacheWallpapers(final List<String> wallpapers) throws InterruptedException {
        Log.v(TAG,"downloadAndCacheWallpapers()");

        CountDownLatch latch = new CountDownLatch(wallpapers.size());

        for (int i = 0; i < wallpapers.size(); ++i) {
            WallpaperCacheRunnable wc = new WallpaperCacheRunnable(mRealmApi, mDiskCache,
                    wallpapers.get(i), latch);
            mUpdateExecutor.execute(wc);
        }

        // wait for all job is down
        latch.await();
        Log.v(TAG, "wallpaper cache updated done");
    }

}
