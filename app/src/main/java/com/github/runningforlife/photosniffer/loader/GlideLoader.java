package com.github.runningforlife.photosniffer.loader;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestListener;
import com.github.runningforlife.photosniffer.ui.adapter.ImagePagerAdapter;

import java.text.DecimalFormat;


/**
 * image loader using Glide
 */

public class GlideLoader {

    public static void load(Context context, ImagePagerAdapter.ImageLoaderListener listener, String url,
                            Priority priority, int w, int h) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .listener(listener)
                .priority(priority)
                .crossFade()
                .thumbnail((float)0.3)
                .into(w,h);
    }

    public static void load(Context context, String url, GlideLoaderListener listener,
                            Priority priority, int w, int h){
        Glide.with(context)
             .load(url)
             .asBitmap()
             .priority(priority)
             .listener(listener)
             .centerCrop()
             .crossFade()
             .into(w,h);
    }

    public static void downloadOnly(Context context, String url, RequestListener<String,Bitmap> listener,
                                    Priority priority, int w, int h){
        Glide.with(context)
                .load(url)
                .asBitmap()
                .priority(priority)
                .listener(listener)
                .into(w,h);
    }

    public static void pauseRequest(Context context){
        Glide.with(context)
             .pauseRequests();
    }
}
