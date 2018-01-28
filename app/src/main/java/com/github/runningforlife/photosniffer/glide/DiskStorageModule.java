package com.github.runningforlife.photosniffer.glide;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 *  a module to setup our own glide params
 */

@GlideModule
public class DiskStorageModule extends AppGlideModule {
    private static final String TAG = "DiskStorageModule";
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        Log.d(TAG,"applyOptions()");

        builder.setDiskCache(new DiskLruFactory(context));
    }
}
