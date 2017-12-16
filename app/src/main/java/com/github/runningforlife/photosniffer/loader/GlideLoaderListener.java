package com.github.runningforlife.photosniffer.loader;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.app.AppGlobals;
import com.github.runningforlife.photosniffer.utils.BitmapUtil;

/**
 * Glide loader complete listener
 */

public final class GlideLoaderListener implements RequestListener<String,Bitmap> {
    private static final String TAG = "GlideLoader";

    private ImageView imageView;
    private ImageLoadCallback callback;
    private int mReqWidth;
    private int mReqHeight;
    private ImageView.ScaleType mScaleType;

    public GlideLoaderListener(ImageView view){
        this.imageView = view;
        mScaleType = ImageView.ScaleType.CENTER_CROP;
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

    public void setScaleType(ImageView.ScaleType scaleType){
        mScaleType = scaleType;
    }

    public interface ImageLoadCallback{
        void onImageLoadDone(Object o);
    }

    @TargetApi(21)
    @Override
    public boolean onException(Exception e, String model, com.bumptech.glide.request.target.Target<Bitmap> target, boolean isFirstResource) {
        Log.v(TAG,"onException(): " + e);
        // 404 IO exception
        if(imageView != null) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageResource(R.drawable.ic_photo_grey_24dp);
        }
        if(callback != null){
            callback.onImageLoadDone(e);
        }
        return false;
    }

    @Override
    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
        if(imageView != null) {
            // scale bitmap
            imageView.setScaleType(mScaleType);
            if(mReqHeight >0 && mReqWidth > 0) {
                Bitmap bm = BitmapUtil.scaleToFill(resource,mReqWidth,mReqHeight);
                imageView.setImageBitmap(bm);
            }else{
                imageView.setImageBitmap(resource);
            }
            // get the main color of the image
            Palette palette = Palette.from(resource).generate();
            if(Build.VERSION.SDK_INT >= 23) {
                RippleDrawable rd = (RippleDrawable) imageView.getForeground();
                if(rd != null) {
                    int dc = palette.getDominantColor(Color.DKGRAY);
                    ColorStateList csl = ColorStateList.valueOf(dc);
                    rd.setColor(csl);
                    rd.setColorFilter(dc, PorterDuff.Mode.DST_IN);
                }
            }
        }

        Log.d(TAG,"onResourceReady(): from memory = " + isFromMemoryCache);
        if(callback != null){
            callback.onImageLoadDone(resource);
        }
        return true;
    }
}
