package com.github.runningforlife.photosniffer.loader;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.runningforlife.photosniffer.R;

/**
 * Glide loader complete listener
 */

public final class GlideLoaderListener implements RequestListener<String,Bitmap> {
    private static final String TAG = "GlideLoader";

    private ImageView imageView;
    private ImageLoadCallback callback;
    private ImageView.ScaleType mScaleType;

    private RequestManager mRequestManager;

    public GlideLoaderListener() {}

    public GlideLoaderListener(RequestManager requestManager) {
        mRequestManager = requestManager;
    }

    public GlideLoaderListener(ImageView view){
        this.imageView = view;
        mScaleType = ImageView.ScaleType.CENTER_CROP;
    }

    public void addCallback(ImageLoadCallback callback){
        this.callback = callback;
    }

    public void setScaleType(ImageView.ScaleType scaleType){
        mScaleType = scaleType;
    }

    public interface ImageLoadCallback{
        void onImageLoadDone(Object o);
    }

    @TargetApi(21)
    @Override
    public boolean onException(Exception e, String model, com.bumptech.glide.request.target.Target<Bitmap> target, boolean isFirstResource) {
        Log.v(TAG, "onException(): " + e);
        // 404 IO exception
        if (imageView != null) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageResource(R.drawable.ic_photo_grey_24dp);
        }
        if (callback != null) {
            callback.onImageLoadDone(e);
        }
        return false;
    }

    @Override
    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
        Log.d(TAG,"onResourceReady(): from memory = " + isFromMemoryCache);
        if (callback != null) {
            callback.onImageLoadDone(resource);
        }
        if(imageView != null) {
            // scale bitmap
            imageView.setScaleType(mScaleType);
            imageView.setImageBitmap(resource);
        }
        return true;
    }
}
