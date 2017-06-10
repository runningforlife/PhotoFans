package com.github.runningforlife.photosniffer.loader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by jason on 6/10/17.
 */

public class PicassoLoaderListener implements Target {
    private static final String TAG = "PicassoLoader";

    private ImageView iv;

    public PicassoLoaderListener(ImageView iv){
        this.iv = iv;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        Log.v(TAG,"onBitmapLoader()");
        iv.setImageBitmap(bitmap);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        Log.v(TAG,"onBitmapFailed()");
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}
