package com.github.runningforlife.photosniffer.glide;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.File;

/**
 *  a LRU disk storage to save images
 */

public class DiskLruFactory extends DiskLruCacheFactory {
    private static final String TAG = "DiskLru";
    private static final String DISK_CACHE_DIR = MiscUtil.getGlideCacheDir();
    private static final int DEFAULT_CACHE_SIZE = 1024*1024*100;
    private static int DISK_CACHE_SIZE = getExternalCacheSize();

    DiskLruFactory(Context context) {
        this(context, DISK_CACHE_DIR, DISK_CACHE_SIZE);
    }

    public DiskLruFactory(Context context, int diskCacheSize) {
        this(context, DISK_CACHE_DIR,diskCacheSize);
    }

    DiskLruFactory(final Context context, final String diskCacheName, int diskCacheSize) {
        super(new CacheDirectoryGetter() {
            @Override
            public File getCacheDirectory() {
                File dir = Environment.getExternalStorageDirectory();
                Log.d(TAG,"getCacheDirectory()");
                if(dir == null){
                    dir = context.getFilesDir();
                }

                if(diskCacheName != null){
                    return new File(dir,diskCacheName);
                }

                return dir;
            }
        },diskCacheSize);
    }

    private static int getExternalCacheSize() {
        File file = Environment.getExternalStorageDirectory();

        long freeSpace = (file.getFreeSpace()/1024/1024); //MB
        int wantSpace = (int) (freeSpace/10);

        return wantSpace > DEFAULT_CACHE_SIZE ? DEFAULT_CACHE_SIZE : wantSpace;
    }
}
