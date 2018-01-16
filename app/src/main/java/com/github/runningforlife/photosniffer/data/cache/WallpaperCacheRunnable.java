package com.github.runningforlife.photosniffer.data.cache;


import android.util.Log;
import android.webkit.URLUtil;

import com.github.runningforlife.photosniffer.crawler.processor.ImageSource;
import com.github.runningforlife.photosniffer.utils.UrlUtil;

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
    private String mOriginalUrl;
    private CountDownLatch mCountLatch;
    private OkHttpClient mHttpClient;
    private ArrayBlockingQueue<String> mCachedUrl;

    public WallpaperCacheRunnable(OkHttpClient httpClient, DiskCache cache, String imgUrl, CountDownLatch latch,
                                  ArrayBlockingQueue<String> cachedUrl) {
        mDiskCache = cache;
        mOriginalUrl = imgUrl;
        mImageUrl = imgUrl.startsWith(PIXELS_IMAGE_START) ?
                UrlUtil.buildHighResolutionPixelsUrl(imgUrl, 650) : imgUrl;
        mCountLatch = latch;
        mHttpClient = httpClient;
        mCachedUrl = cachedUrl;
    }

    @Override
    public void run() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        builder.url(mImageUrl)
                .get();
        try {
            Response response = mHttpClient.newCall(builder.build()).execute();
            if (response.isSuccessful()) {
                saveStreamToFile(response.body().bytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // job is done
            mCountLatch.countDown();
        }
    }

    private void saveStreamToFile(byte[] data) {
        Log.v(TAG,"saveStreamToFile()");
        Cache.Entry entry = new Cache.Entry(data, System.currentTimeMillis());
        if (mDiskCache.put(mImageUrl, entry)) {
            mCachedUrl.offer(mOriginalUrl);
        }
    }
}
