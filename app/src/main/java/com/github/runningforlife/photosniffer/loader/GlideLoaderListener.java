package com.github.runningforlife.photosniffer.loader;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * Glide loader complete listener
 */

public final class GlideLoaderListener implements RequestListener<String,Bitmap> {
    private static final String TAG = "GlideLoader";
    private ImageView imageView;
    private ImageLoadCallback callback;

    public GlideLoaderListener(ImageView view){
        this.imageView = view;
    }

    public void addCallback(ImageLoadCallback callback){
        this.callback = callback;
    }

    public interface ImageLoadCallback{
        void onImageLoadDone(Object o);
    }

    @TargetApi(21)
    @Override
    public boolean onException(Exception e, String model, com.bumptech.glide.request.target.Target<Bitmap> target, boolean isFirstResource) {
        Log.v(TAG,"onException(): " + e);
        if(callback != null){
            callback.onImageLoadDone(e);
        }
        return false;
    }

    @Override
    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
        if(imageView != null) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(resource);
        }
        Log.v(TAG,"onResourceReady(): from memory = " + isFromMemoryCache);
        if(callback != null){
            callback.onImageLoadDone(resource);
        }
        return false;
    }
}
