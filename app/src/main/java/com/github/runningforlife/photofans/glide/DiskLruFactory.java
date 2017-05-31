package com.github.runningforlife.photofans.glide;

import android.content.Context;

import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.github.runningforlife.photofans.app.AppGlobals;

import java.io.File;

/**
 *  a LRU disk storage to save images
 */

public class DiskLruFactory extends DiskLruCacheFactory{
    public static final String DISK_CACHE_DIR = "PhotoFans/Pictures/Cache/";
    public static final int DISK_CACHE_SIZE = 1024*1024*100;

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
                File dir = context.getFilesDir();
                if(dir == null){
                    return null;
                }

                if(diskCacheName != null){
                    return new File(dir,diskCacheName);
                }

                return dir;
            }
        },diskCacheSize);
    }
}
