package com.github.runningforlife.photosniffer.loader;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.runningforlife.photosniffer.ui.adapter.ImagePagerAdapter;

import java.io.File;
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
                .thumbnail((float)0.3)
                .into(w,h);
    }

    public static void load(Fragment fragment, String url, GlideLoaderListener listener,
                            Priority priority, int w, int h){
        Glide.with(fragment)
             .load(url)
             .asBitmap()
             .priority(priority)
             .listener(listener)
             .centerCrop()
             .crossFade()
             .thumbnail((float)0.3)
             .into(w,h);
    }

    public static void downloadOnly(Context context, String url, RequestListener<String,Bitmap> listener,
                                    Priority priority, int w, int h, boolean isWallpaper){
        if (isWallpaper) {
            Glide.with(context)
                    .load(url)
                    .asBitmap()
                    .centerCrop()
                    .priority(priority)
                    .listener(listener)
                    .into(w, h);
        } else {
            Glide.with(context)
                    .load(url)
                    .asBitmap()
                    .priority(priority)
                    .listener(listener)
                    .into(w, h);
        }
    }

    public static void downloadOriginalImage(Context context, String url, Target<File> target) {
        Glide.with(context)
             .load(url)
             .downloadOnly(target);
    }

    public static void pauseRequest(Context context){
        Glide.with(context)
             .pauseRequests();
    }
}
