package com.github.runningforlife.photosniffer.loader;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.github.runningforlife.photosniffer.ui.adapter.ImagePagerAdapter;


/**
 * image loader using Glide
 */

public class GlideLoader {

    public static void load(Context context, ImagePagerAdapter.ImageLoaderListener listener, String url, int w, int h) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .listener(listener)
                .crossFade()
                .thumbnail((float)0.3)
                .into(w,h);
    }

    public static void load(Context context, String url, GlideLoaderListener listener, int w, int h){
        Glide.with(context)
             .load(url)
             .asBitmap()
             .listener(listener)
             .centerCrop()
             .crossFade()
             .into(w,h);
    }

    public static void downloadOnly(Context context, String url, RequestListener<String,Bitmap> listener,
                                    int w, int h){
        Glide.with(context)
                .load(url)
                .asBitmap()
                .listener(listener)
                .into(w,h);
    }
}
