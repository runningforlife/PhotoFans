package com.github.runningforlife.photosniffer.data.cache;

import android.text.TextUtils;
import android.util.Log;


import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * a disk Cache to Cache wallpapers
 */

public class DiskCache implements Cache {

    private static final String TAG = "DiskCache";
    /** default size is 50MB */
    private static final int DEFAULT_CACHE_SIZE = 50 * 1024 * 1024;
    /** default imag format */
    private static final String DEFAULT_IMAGE_FORMAT = ".png";
    private static final String CACHE_IMAGE_PREFIX = "img-";

    /** all Cache entries info */
    private Map<String, CacheInfo> mEntries = new LinkedHashMap<>(30, 0.75f, true);
    /** all cache file name */
    private Set<String> mImageFiles = new HashSet<>();
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
    public synchronized void put(String url, Entry entry) {
        // make sure Cache is under limited space
        trim(entry.data.length);

        String key = getCacheKey(url);
        File file = getFileNameByKey(key);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(entry.data);
            bos.close();

            CacheInfo cacheInfo = new CacheInfo(key, entry.data.length);
            cacheInfo.lastModified = entry.lastModified;
            putEntry(key, cacheInfo);

            mImageFiles.add(file.getAbsolutePath());
            Log.v(TAG,"put(): Cache image done");
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!file.delete()) {
            Log.e(TAG,"fail to delete file:" + file.getAbsolutePath());
        }
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
        String key = getCacheKey(url);
        return getFileNameByKey(key).getAbsolutePath();
    }

    @Override
    public synchronized void remove(String url) {
        String key = getCacheKey(url);
        File file = getFileNameByKey(key);

        mImageFiles.remove(file.getAbsolutePath());

        if (!file.delete()) {
            Log.e(TAG,"fail to delete file:" + file.getAbsolutePath());
        }

        removeEntry(key);
    }

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

        mImageFiles.clear();
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
        return mImageFiles.contains(url);
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

        for (File file : files) {
            mImageFiles.add(file.getAbsolutePath());

            CacheInfo ci = new CacheInfo(file.getName(), file.length());
            ci.lastModified = file.lastModified();

            putEntry(ci.key, ci);
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
