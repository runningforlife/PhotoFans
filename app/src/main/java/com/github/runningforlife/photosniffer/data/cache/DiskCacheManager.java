package com.github.runningforlife.photosniffer.data.cache;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jason on 12/17/17.
 */

public class DiskCacheManager implements CacheApi {

    private DiskCache mDiskCache;
    private ExecutorService mCacheExecutors;
    private EventHandler mEventHandler;

    private static DiskCacheManager sInstance = new DiskCacheManager();

    public static CacheApi getInstance() {
        return sInstance;
    }

    private DiskCacheManager() {
        mDiskCache = new DiskCache(new File(MiscUtil.getWallpaperCacheDir()));
        mCacheExecutors = Executors.newCachedThreadPool();

        HandlerThread ht = new HandlerThread("DiskCacheMgr");
        ht.start();

        mEventHandler = new EventHandler(ht.getLooper());

        CacheActionRunnable car = new CacheActionRunnable(EVENT_PUT);
        sendEvent(car);

    }

    @Override
    public void put(String url, Bitmap bitmap) {
        CacheActionRunnable car = new CacheActionRunnable(url, EVENT_PUT, bitmap);
        sendEvent(car);
    }

    @Override
    public void put(String url, Cache.Entry entry) {
        CacheActionRunnable car = new CacheActionRunnable(url, null, EVENT_PUT, entry);
        sendEvent(car);
    }

    @Override
    public void get(String url, CacheCallback callback) {
        CacheActionRunnable car = new CacheActionRunnable(url, callback, EVENT_GET, null);
        sendEvent(car);
    }

    @Override
    public Cache.Entry get(String url) {
        return mDiskCache.get(url);
    }

    @Override
    public String getFilePath(String url) {
        return mDiskCache.getFilePath(url);
    }

    @Override
    public void remove(String url) {
        CacheActionRunnable car = new CacheActionRunnable(url, null, EVENT_REMOVE, null);
        sendEvent(car);
    }

    @Override
    public void clear() {
        CacheActionRunnable car = new CacheActionRunnable(null, null, EVENT_CLEAR, null);
        sendEvent(car);
    }

    @Override
    public boolean isExist(String url) {
        return mDiskCache.isExist(url);
    }

    private void sendEvent(Runnable runnable) {
        Message message = mEventHandler.obtainMessage();
        message.obj = runnable;
        message.sendToTarget();
    }

    final class EventHandler extends Handler {
        EventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            if (message.obj != null) {
                Runnable runnable = (Runnable) message.obj;
                mCacheExecutors.submit(runnable);
            }
        }
    }

    final class CacheActionRunnable implements Runnable {
        String url;
        CacheCallback callback;
        @CacheAction int action;
        Cache.Entry entry;
        Bitmap bitmap;

        CacheActionRunnable(@CacheAction int action) {
            this.action = action;
        }

        CacheActionRunnable(String url, @CacheAction int action, Bitmap bitmap) {
            this.url = url;
            this.action = action;
            this.bitmap = bitmap;
        }

        CacheActionRunnable(String url, CacheCallback callback,
                            @CacheAction int action, Cache.Entry entry) {
            this.url = url;
            this.callback = callback;
            this.action = action;
            this.entry = entry;
        }

        @Override
        public void run() {
            switch (action) {
                case EVENT_INIT:
                    mDiskCache.initialize();
                    break;
                case EVENT_GET:
                    Cache.Entry entry1 = mDiskCache.get(url);
                    if (this.callback != null) {
                        callback.onGetEntryDone(entry1);
                    }
                    break;
                case EVENT_CLEAR:
                    mDiskCache.clear();
                    break;
                case EVENT_PUT:
                    if (bitmap != null) {
                        ByteArrayOutputStream bos;
                        if ( Build.VERSION.SDK_INT >= 19) {
                            bos = new ByteArrayOutputStream(bitmap.getAllocationByteCount());
                        } else {
                            bos = new ByteArrayOutputStream(bitmap.getByteCount());
                        }
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        Cache.Entry entry2 = new Cache.Entry(bos.toByteArray(), System.currentTimeMillis());
                        mDiskCache.put(url, entry2);
                    }
                    if (entry != null) {
                        mDiskCache.put(url, entry);
                    }
                    break;
                case EVENT_REMOVE:
                    mDiskCache.remove(url);
                    break;
            }
        }
    }
}
