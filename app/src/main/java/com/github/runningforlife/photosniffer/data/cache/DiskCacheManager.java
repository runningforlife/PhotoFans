package com.github.runningforlife.photosniffer.data.cache;

import android.content.Context;

import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jason on 12/17/17.
 */

public class DiskCacheManager implements CacheApi {

    private DiskCache mDiskCache;
    private ExecutorService mCacheExecutors;

    private static DiskCacheManager sInstance = new DiskCacheManager();

    public static CacheApi getInstance() {
        return sInstance;
    }

    private DiskCacheManager() {
        mDiskCache = new DiskCache(new File(MiscUtil.getWallpaperCacheDir()));
        mCacheExecutors = Executors.newSingleThreadExecutor();
        CacheActionRunnable car = new CacheActionRunnable(null, null,
                ACTION_INIT, null);
        mCacheExecutors.submit(car);
    }

    @Override
    public void put(String url, Cache.Entry entry) {
        CacheActionRunnable car = new CacheActionRunnable(url, null, ACTION_PUT, entry);
        mCacheExecutors.submit(car);
    }

    @Override
    public void get(String url, CacheCallback callback) {
        CacheActionRunnable car = new CacheActionRunnable(url, callback, ACTION_GET, null);
        mCacheExecutors.submit(car);
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
        CacheActionRunnable car = new CacheActionRunnable(url, null, ACTION_REMOVE, null);
        mCacheExecutors.submit(car);
    }

    @Override
    public void clear() {
        CacheActionRunnable car = new CacheActionRunnable(null, null, ACTION_CLEAR, null);
        mCacheExecutors.submit(car);
    }

    @Override
    public boolean isExist(String url) {
        return mDiskCache.isExist(url);
    }

    final class CacheActionRunnable implements Runnable {
        String url;
        CacheCallback callback;
        @CacheAction String action;
        Cache.Entry entry;

        CacheActionRunnable(String url, CacheCallback callback,
                            @CacheAction String action, Cache.Entry entry) {
            this.url = url;
            this.callback = callback;
            this.action = action;
            this.entry = entry;
        }

        @Override
        public void run() {
            switch (action) {
                case ACTION_INIT:
                    mDiskCache.initialize();
                    break;
                case ACTION_GET:
                    Cache.Entry entry1 = mDiskCache.get(url);
                    if (this.callback != null) {
                        callback.onGetEntryDone(entry1);
                    }
                    break;
                case ACTION_CLEAR:
                    mDiskCache.clear();
                    break;
                case ACTION_PUT:
                    mDiskCache.put(url, entry);
                    break;
                case ACTION_REMOVE:
                    mDiskCache.remove(url);
                    break;
            }
        }
    }
}
