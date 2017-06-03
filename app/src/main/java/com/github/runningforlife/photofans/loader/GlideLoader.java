package com.github.runningforlife.photofans.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.ui.adapter.ImagePagerAdapter;


/**
 * image loader using Glide
 */

public class GlideLoader {

    public static void load(Context context, ImagePagerAdapter.ImageLoaderListener listener, String url, int w, int h) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .error(R.drawable.ic_mood_bad_grey_24dp)
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
             .error(R.drawable.ic_mood_bad_grey_24dp)
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
