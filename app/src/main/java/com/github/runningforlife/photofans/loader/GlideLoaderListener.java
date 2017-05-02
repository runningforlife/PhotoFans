package com.github.runningforlife.photofans.loader;

import android.annotation.TargetApi;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.app.AppGlobals;

/**
 * Glide loader complete listener
 */

public final class GlideLoaderListener implements RequestListener<String,GlideDrawable> {
    private static final String TAG = "GlideLoader";
    private ImageView image;

    public GlideLoaderListener(ImageView view){
        this.image = view;
    }

    @TargetApi(21)
    @Override
    public boolean onException(Exception e, String model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFirstResource) {
        Log.v(TAG,"onException(): " + e);
        image.setImageDrawable(AppGlobals.getInstance().getDrawable(R.drawable.ic_mood_bad_grey_24dp));
        return false;
    }

    @Override
    public boolean onResourceReady(GlideDrawable resource, String model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
        Log.v(TAG,"onResourceReady(): from memory = " + isFromMemoryCache);
        image.setImageDrawable(resource);

        return false;
    }
}
