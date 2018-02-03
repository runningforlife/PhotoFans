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

import com.github.runningforlife.photosniffer.data.cache.DiskCache;
import com.github.runningforlife.photosniffer.data.cache.WallpaperCacheRunnable;
import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * fetch images from web and cache them
 */

public class WallpaperUpdaterService extends Service {
    private static final String TAG = "WallpaperUpdaterService";

    private DiskCache mDiskCache;
    private ExecutorService mUpdateExecutor;
    private volatile ServiceHandler mHandler;
    private volatile Looper mLooper;
    private OkHttpClient mHttpClient;


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

        mDiskCache = new DiskCache(new File(MiscUtil.getWallpaperCacheDir()));

        mUpdateExecutor = Executors.newFixedThreadPool(2);

        mHttpClient = MiscUtil.buildOkHttpClient();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mLooper.quit();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG,"onStartCommand()");
        Message message = mHandler.obtainMessage();
        message.obj = intent;
        message.sendToTarget();
        return START_STICKY;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onHandleIntent(Intent intent) {
        Log.v(TAG,"onHandleIntent()");
        // every time we try to download & cache 15 images
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
        if (MiscUtil.isConnected(getApplicationContext())) {
            CountDownLatch latch = new CountDownLatch(wallpapers.size());
            ArrayBlockingQueue<String> cachedUrls = new ArrayBlockingQueue<>(wallpapers.size());
            for (int i = 0; i < wallpapers.size(); ++i) {
                WallpaperCacheRunnable wc = new WallpaperCacheRunnable(mHttpClient,mDiskCache, wallpapers.get(i), latch ,
                        cachedUrls);
                mUpdateExecutor.submit(wc);
            }
            // wait for all job is down
            try {
                latch.await(10, TimeUnit.SECONDS);
            } catch (Exception e) {

            }
            // insert to realm
            List<ImageRealm> imageRealms = new ArrayList<>(cachedUrls.size());
            for (String url : cachedUrls) {
                ImageRealm ir = new ImageRealm();
                ir.setUrl(mDiskCache.getFilePath(url));
                ir.setTimeStamp(System.currentTimeMillis());
                ir.setIsWallpaper(true);
                ir.setIsFavor(false);
                ir.setUsed(true);

                imageRealms.add(ir);
            }

            RealmApi realmApi = RealmApiImpl.getInstance();
            try {
                realmApi.insertAsync(imageRealms);
                String[] imageUrl = new String[cachedUrls.size()];
                cachedUrls.toArray(imageUrl);
                realmApi.deleteSync(Arrays.asList(imageUrl));
            } finally {
                realmApi.closeRealm();
                cachedUrls.clear();
            }
            Log.v(TAG, "wallpaper cache updated done");
        }
    }
}
