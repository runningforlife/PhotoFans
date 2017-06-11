package com.github.runningforlife.photosniffer.loader;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.runningforlife.photosniffer.utils.BitmapUtil;
import com.github.runningforlife.photosniffer.utils.DisplayUtil;

/**
 * Glide loader complete listener
 */

public final class GlideLoaderListener implements RequestListener<String,Bitmap> {
    private static final String TAG = "GlideLoader";

    private ImageView imageView;
    private ImageLoadCallback callback;
    private int mReqWidth;
    private int mReqHeight;

    public GlideLoaderListener(ImageView view){
        this.imageView = view;
    }

    public void addCallback(ImageLoadCallback callback){
        this.callback = callback;
    }

    public void setReqWidth(int width){
        mReqWidth = width;
    }

    public void setReqHeight(int height){
        mReqHeight = height ;
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
            // scale bitmap
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if(mReqHeight >0 && mReqWidth > 0) {
                Bitmap bm = BitmapUtil.scaleToFill(resource,mReqWidth,mReqHeight);
                imageView.setImageBitmap(bm);
            }else{
                imageView.setImageBitmap(resource);
            }
        }

        Log.d(TAG,"onResourceReady(): from memory = " + isFromMemoryCache);
        if(callback != null){
            callback.onImageLoadDone(resource);
        }
        return false;
    }
}
