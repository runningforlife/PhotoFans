package com.github.runningforlife.photofans.loader;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.app.AppGlobals;

/**
 * Glide loader complete listener
 */

public final class GlideLoaderListener implements RequestListener<String,Bitmap> {
    private static final String TAG = "GlideLoader";
    private ImageView image;
    private ImageLoadCallback callback;

    public GlideLoaderListener(ImageView view){
        this.image = view;
    }

    public void addCallback(ImageLoadCallback callback){
        this.callback = callback;
    }

    public interface ImageLoadCallback{
        void onImageLoadDone(Bitmap bitmap);
    }

    @TargetApi(21)
    @Override
    public boolean onException(Exception e, String model, com.bumptech.glide.request.target.Target<Bitmap> target, boolean isFirstResource) {
        Log.v(TAG,"onException(): " + e);
        image.setImageDrawable(AppGlobals.getInstance().getDrawable(R.drawable.ic_mood_bad_grey_24dp));
        return false;
    }

    @Override
    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
        image.setImageBitmap(resource);
        Log.v(TAG,"onResourceReady(): from memory = " + isFromMemoryCache);
        if(callback != null){
            callback.onImageLoadDone(resource);
        }
        return false;
    }
}
