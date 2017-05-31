package com.github.runningforlife.photofans.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.module.GlideModule;

/**
 *  a module to setup our own glide params
 */

public class DiskStorageModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888)
                .setDiskCache(new DiskLruFactory(context));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        // do nothing
    }
}
