package com.github.runningforlife.photosniffer.data.cache;


import android.util.Log;


import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import java.io.IOException;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

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
    private OkHttpClient mHttpClient;
    private RealmApi mRealmApi;

    public WallpaperCacheRunnable(RealmApi realmApi, DiskWallpaperCache cache, String imgUrl, CountDownLatch latch) {
        mRealmApi = realmApi;
        mDiskCache = cache;
        mImageUrl = imgUrl;
        mCountLatch = latch;
        mHttpClient = MiscUtil.buildOkHttpClient();
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
                // update realm
                updateRealm(mImageUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // job is done
        mCountLatch.countDown();
    }


    private void saveStreamToFile(byte[] data) {
        Log.v(TAG,"saveStreamToFile()");
        cache.Entry entry = new cache.Entry(data, System.currentTimeMillis());
        mDiskCache.put(mImageUrl, entry);
    }

    private void updateRealm(String url) {
        Log.v(TAG,"updateRealm()");
        HashMap<String, String> params = new HashMap<>();
        params.put("mUrl", url);

        HashMap<String, String> newValues = new HashMap<>();
        String fileKey = mDiskCache.getCacheKey(url);
        newValues.put("mUrl", mDiskCache.getFileName(fileKey));
        newValues.put("mIsUsed", Boolean.toString(Boolean.TRUE));
        newValues.put("mIsWallpaper", Boolean.toString(true));
        //newValues.put("mIsCached", Boolean.toString(true));
        mRealmApi.updateAsync(ImageRealm.class, params, newValues);
    }
}
