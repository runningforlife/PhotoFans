package com.github.runningforlife.photosniffer.data.cache;


import android.util.Log;

import com.github.runningforlife.photosniffer.utils.OkHttpUtil;

import java.io.IOException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import okhttp3.OkHttpClient;
import okhttp3.Response;

import static com.github.runningforlife.photosniffer.crawler.processor.ImageSource.PIXELS_IMAGE_START;

/**
 * Created by jason on 12/9/17.
 */

public class WallpaperCacheRunnable implements Runnable {
    private static final String TAG = "WallpaperCache";

    private DiskCache mDiskCache;
    private String mImageUrl;
    private CountDownLatch mCountLatch;
    private OkHttpClient mHttpClient;
    private ArrayBlockingQueue<String> mCachedUrl;

    public WallpaperCacheRunnable(OkHttpClient httpClient, DiskCache cache, String imgUrl, CountDownLatch latch,
                                  ArrayBlockingQueue<String> cachedUrl) {
        mDiskCache = cache;
        mImageUrl = imgUrl;
        mCountLatch = latch;
        mHttpClient = httpClient;
        mCachedUrl = cachedUrl;
    }

    @Override
    public void run() {
        try {
            Response response = mHttpClient.newCall(OkHttpUtil.buildHttpRequest(mImageUrl)).execute();
            if (response.isSuccessful()) {
                saveStreamToFile(response.body().bytes());
            }
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            // job is done
            mCountLatch.countDown();
        }
    }

    private void saveStreamToFile(byte[] data) {
        Log.v(TAG,"saveStreamToFile()");
        Cache.Entry entry = new Cache.Entry(data, System.currentTimeMillis());
        if (mDiskCache.put(mImageUrl, entry)) {
            mCachedUrl.offer(mImageUrl);
        }
    }
}
