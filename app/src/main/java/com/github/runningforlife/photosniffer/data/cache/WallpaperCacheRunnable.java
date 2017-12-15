package com.github.runningforlife.photosniffer.data.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.app.AppGlobals;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.PicassoLoader;
import com.github.runningforlife.photosniffer.utils.BitmapUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by jason on 12/9/17.
 */

public class WallpaperCacheRunnable implements Runnable {
    private static final String TAG = "WallpaperCache";

    private DiskWallpaperCache mDiskCache;
    private String mImageUrl;
    private CountDownLatch mCountLatch;
    private ImageLoadCallback mLoadCb;
    private OkHttpClient mHttpClient;

    public interface ImageLoadCallback {
        void onLoadDone(String url, boolean isOk);
    }

    public WallpaperCacheRunnable(DiskWallpaperCache cache, String imgUrl, CountDownLatch latch) {
        mDiskCache = cache;
        mImageUrl = imgUrl;
        mCountLatch = latch;
        mHttpClient = new OkHttpClient();
    }

    public void setLoadCallback(ImageLoadCallback callback) {
        mLoadCb = callback;
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
                mLoadCb.onLoadDone(mImageUrl, true);
            } else {
                mLoadCb.onLoadDone(mImageUrl, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            mLoadCb.onLoadDone(mImageUrl, false);
        }
        // job is done
        mCountLatch.countDown();
    }


    private void saveStreamToFile(byte[] data) {
        Log.v(TAG,"saveStreamToFile()");
        cache.Entry entry = new cache.Entry(data, System.currentTimeMillis());
        mDiskCache.put(mImageUrl, entry);
    }
}
