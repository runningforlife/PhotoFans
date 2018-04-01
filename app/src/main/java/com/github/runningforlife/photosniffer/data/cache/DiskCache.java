package com.github.runningforlife.photosniffer.data.cache;

import android.text.TextUtils;
import android.util.Log;


import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * a disk Cache to Cache wallpapers
 */

public class DiskCache implements Cache {

    private static final String TAG = "DiskCache";
    /** default size is 50MB */
    private static final int DEFAULT_CACHE_SIZE = 100 * 1024 * 1024;
    /** default imag format */
    private static final String DEFAULT_IMAGE_FORMAT = ".png";
    private static final String CACHE_IMAGE_PREFIX = "img";

    /** all Cache entries info */
    private Map<String, CacheInfo> mEntries = new LinkedHashMap<>(30, 0.75f, true);
    private File mRootDir;
    private int mMaxSize;
    private int mTotalSize;

    public DiskCache(File rootDir, int maxSize) {
        mRootDir = rootDir;
        mMaxSize = maxSize;

        initialize();
    }

    public DiskCache(File rootDir) {
        this(rootDir, DEFAULT_CACHE_SIZE);
    }

    @Override
    public synchronized boolean put(String url, Entry entry) {
        // make sure Cache is under limited space
        String key = getCacheKey(url);
        File file = getFileNameByKey(key);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(entry.data);
            bos.close();

            CacheInfo cacheInfo = new CacheInfo(key, entry.data.length);
            cacheInfo.lastModified = entry.lastModified;
            putEntry(key, cacheInfo);
            Log.v(TAG,"put(): Cache image done");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!file.delete()) {
            Log.e(TAG,"fail to delete file:" + file.getAbsolutePath());
        }

        return false;
    }

    @Override
    public synchronized Entry get(String url) {
        String key = getCacheKey(url);
        CacheInfo cacheInfo = mEntries.get(key);
        if (cacheInfo == null)
            return null;

        File file = getFileNameByKey(key);
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            byte[] data = new byte[(int)file.length()];
            bis.read(data);
            bis.close();

            return new Entry(data, file.lastModified());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getFilePath(String url) {
        if (TextUtils.isEmpty(url)) return null;
        String key = getCacheKey(url);
        return getFileNameByKey(key).getAbsolutePath();
    }

    @Override
    public synchronized void remove(String url) {
        String key = getCacheKey(url);
        File file = getFileNameByKey(key);

        if (file.exists() && !file.delete()) {
            Log.e(TAG,"fail to delete file:" + file.getAbsolutePath());
        }

        removeEntry(key);
    }

    // based on time or images number
    private void trim(int wantedSpace) {
        if (mTotalSize + wantedSpace < mMaxSize) {
            return;
        }

        Iterator<Map.Entry<String,CacheInfo>> iterator = mEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CacheInfo> entry = iterator.next();
            String key = entry.getKey();
            CacheInfo cacheInfo = entry.getValue();
            File file = getFileNameByKey(key);

            if(!file.delete()) {
                Log.e(TAG,"could not delete Cache entry key=" + key + ", file=" + file.getAbsolutePath());
            } else {
                mTotalSize -= cacheInfo.size;
            }

            iterator.remove();

            if (mTotalSize + wantedSpace < mMaxSize) {
                break;
            }
        }
    }

    @Override
    public synchronized void clear() {
        File[] files = mRootDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }

        mEntries.clear();
        mTotalSize = 0;

        Log.v(TAG,"image Cache cleared");
    }


    /** get the Cache key by image url */
    public static String getCacheKey(String imageUrl) {
        int fistHalf = imageUrl.length()/2;
        String key = String.valueOf(imageUrl.substring(0, fistHalf).hashCode());
        key += String.valueOf(imageUrl.substring(fistHalf).hashCode());
        return key;
    }

    @Override
    public boolean isExist(String url) {
        String key = getCacheKey(url);
        return mEntries.containsKey(key);
    }

    @Override
    public synchronized void initialize() {
        if (!mRootDir.exists()) {
            if (!mRootDir.mkdirs()) {
                Log.e(TAG,"fail to create disk Cache dir:" + mRootDir.getAbsolutePath());
            }
        }

        File[] files = mRootDir.listFiles();
        if (files == null) return;

        List<String> removed = new ArrayList<>();
        for (File file : files) {
            if (!file.exists()) {
                removed.add(file.getAbsolutePath());
            } else {
                CacheInfo ci = new CacheInfo(file.getName(), file.length());
                ci.lastModified = file.lastModified();

                putEntry(ci.key, ci);
            }
        }

        if (removed.size() > 0) {
            RealmApi realmApi = RealmApiImpl.getInstance();
            realmApi.deleteSync(removed);
            realmApi.closeRealm();
        }
    }

    private void putEntry(String key, CacheInfo cacheInfo) {
        if (TextUtils.isEmpty(key)) return;

        if (!mEntries.containsKey(key)) {
            mTotalSize += cacheInfo.size;
        } else {
            CacheInfo old = mEntries.get(key);
            mTotalSize += cacheInfo.size - old.size;
        }

        mEntries.put(key, cacheInfo);
    }

    private void removeEntry(String key) {
        CacheInfo cacheInfo = mEntries.remove(key);

        if (cacheInfo != null) {
            mTotalSize -= cacheInfo.size;
        }
    }

    private File getFileNameByKey(String key) {
        String fileName = CACHE_IMAGE_PREFIX + key + DEFAULT_IMAGE_FORMAT;
        return new File(mRootDir, fileName);
    }

    private static class CacheInfo {
        /** key to get Cache item, here is Cache file name */
        String key;

        /* Cache item size */
        long size;

        /** last modified time */
        long lastModified;

        CacheInfo(String key, long size) {
            this.key = key;
            this.size = size;
        }
    }
}
