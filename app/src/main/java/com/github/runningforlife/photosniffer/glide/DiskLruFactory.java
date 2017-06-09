package com.github.runningforlife.photosniffer.glide;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;

import java.io.File;

/**
 *  a LRU disk storage to save images
 */

public class DiskLruFactory extends DiskLruCacheFactory{
    private static final String TAG = "DiskLru";
    static final String DISK_CACHE_DIR = "PhotoSniffer/Pictures/Cache/";
    static final int DISK_CACHE_SIZE = 1024*1024*100;

    public DiskLruFactory(Context context) {
        this(context, DISK_CACHE_DIR, DISK_CACHE_SIZE);
    }

    public DiskLruFactory(Context context, int diskCacheSize) {
        this(context, DISK_CACHE_DIR,diskCacheSize);
    }

    public DiskLruFactory(final Context context, final String diskCacheName, int diskCacheSize) {
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
}
