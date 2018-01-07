package com.github.runningforlife.photosniffer.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestListener;


/**
 * image loader using Glide
 */

public class GlideLoader {

    public static void load(RequestManager requestManager, String url, GlideLoaderListener listener,
                            Priority priority, int w, int h, ImageView.ScaleType scaleType){
        BitmapTypeRequest<String> btr = (BitmapTypeRequest<String>) requestManager
                .load(url)
                .asBitmap()
                .priority(priority)
                .listener(listener);
        if (scaleType == ImageView.ScaleType.CENTER_CROP) {
            btr.centerCrop()
                    .crossFade()
                    .dontAnimate()
                    .into(w,h);
        } else {
            btr.fitCenter()
                    .dontAnimate()
                    .dontTransform()
                    .crossFade()
                    .into(w,h);
        }
    }

    public static void load(Context context, String url, GlideLoaderListener listener,
                            Priority priority, int w, int h, ImageView.ScaleType scaleType){
        BitmapTypeRequest<String> btr = (BitmapTypeRequest<String>) Glide.with(context)
                .load(url)
                .asBitmap()
                .priority(priority)
                .listener(listener);
        if (scaleType == ImageView.ScaleType.CENTER_CROP) {
             btr.centerCrop()
                .crossFade()
                .dontAnimate()
                .into(w,h);
        } else {
             btr.fitCenter()
                .dontAnimate()
                .dontTransform()
                .crossFade()
                .into(w,h);
        }
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
             .dontAnimate()
             .dontTransform()
             .into(w,h);
    }

    public static void downloadOnly(Context context, String url, RequestListener<String,Bitmap> listener,
                                    Priority priority, int w, int h, boolean isWallpaper) {
        if (isWallpaper) {
            Glide.with(context)
                    .load(url)
                    .asBitmap()
                    .centerCrop()
                    .priority(priority)
                    .listener(listener)
                    .dontAnimate()
                    .dontTransform()
                    .into(w, h);
        } else {
            Glide.with(context)
                    .load(url)
                    .asBitmap()
                    .priority(priority)
                    .listener(listener)
                    .dontAnimate()
                    .dontTransform()
                    .into(w, h);
        }
    }

    public static void pauseRequest(Context context){
        Glide.with(context)
             .pauseRequests();
    }
}
