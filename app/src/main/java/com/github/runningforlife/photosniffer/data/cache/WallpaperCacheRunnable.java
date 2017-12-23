package com.github.runningforlife.photosniffer.data.cache;


import android.app.backup.BackupDataOutput;
import android.util.Log;


import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
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

    private DiskCache mDiskCache;
    private String mImageUrl;
    private CountDownLatch mCountLatch;
    private OkHttpClient mHttpClient;
    private RealmApi mRealmApi;

    public WallpaperCacheRunnable(DiskCache cache, String imgUrl, CountDownLatch latch) {
        mDiskCache = cache;
        mImageUrl = imgUrl;
        mCountLatch = latch;
        mHttpClient = MiscUtil.buildOkHttpClient();
    }

    @Override
    public void run() {
        mRealmApi = RealmApiImpl.getInstance();

        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        builder.url(mImageUrl)
                .get();
        try {
            Response response = mHttpClient.newCall(builder.build()).execute();
            if (response.isSuccessful()) {
                // update realm
                updateRealm(mImageUrl);
                saveStreamToFile(response.body().bytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mRealmApi.closeRealm();
            // job is done
            mCountLatch.countDown();
        }
    }


    private void saveStreamToFile(byte[] data) {
        Log.v(TAG,"saveStreamToFile()");
        Cache.Entry entry = new Cache.Entry(data, System.currentTimeMillis());
        mDiskCache.put(mImageUrl, entry);
    }

    private void updateRealm(String url) {
        Log.v(TAG,"updateRealm()");
        HashMap<String, String> params = new HashMap<>();
        params.put("mUrl", url);

        HashMap<String, String> newValues = new HashMap<>();
        newValues.put("mUrl", mDiskCache.getFilePath(url));
        newValues.put("mIsUsed", Boolean.toString(Boolean.TRUE));
        newValues.put("mIsWallpaper", Boolean.toString(true));
        newValues.put("mIsCached", Boolean.toString(Boolean.TRUE));
        newValues.put("mTimeStamp", Long.toString(System.currentTimeMillis()));
        //newValues.put("mIsCached", Boolean.toString(true));
        mRealmApi.updateAsync(ImageRealm.class, params, newValues);
    }
}
