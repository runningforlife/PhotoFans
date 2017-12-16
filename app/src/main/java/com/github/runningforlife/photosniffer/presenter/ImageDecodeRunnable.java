package com.github.runningforlife.photosniffer.presenter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.DecodeFormat;

import java.io.File;

/**
 * a runnable to decode image
 */

public class ImageDecodeRunnable implements Runnable {

    private String mDecodeFile;
    private int mWidth;
    private int mHeight;
    private DecodeCallback mCallback;

    public interface DecodeCallback {
        void onDecodeDone(Bitmap bitmap);
    }

    public ImageDecodeRunnable(String imgName, int w, int h) {
        mDecodeFile = imgName;
        mWidth = w;
        mHeight = h;
    }

    public void setDecodeCallback(@Nullable DecodeCallback callback) {
        mCallback = callback;
    }

    @Override
    public void run() {
        File file = new File(mDecodeFile);
        Bitmap bitmap = null;
        if (file.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.outWidth = mWidth;
            options.outHeight = mHeight;
            options.inScaled = true;

            bitmap = BitmapFactory.decodeFile(mDecodeFile, options);
        }

        if (mCallback != null) {
            mCallback.onDecodeDone(bitmap);
        }
    }
}
