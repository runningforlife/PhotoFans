package com.github.runningforlife.photosniffer.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.github.runningforlife.photosniffer.app.AppGlobals;
import com.github.runningforlife.photosniffer.utils.BitmapUtil;
import com.github.runningforlife.photosniffer.utils.MediaStoreUtil;
import com.github.runningforlife.photosniffer.utils.ThreadUtil;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * a runnable to save image
 */

public class ImageSaveRunnable implements Runnable {
    private static final String TAG = "ImageSave";

    private Bitmap bitmap;
    private String name;
    private ImageSaveCallback callback;

    public interface ImageSaveCallback{
        void onImageSaveDone(String path);
    }

    public ImageSaveRunnable(Bitmap bitmap, String name){
        this.bitmap = bitmap;
        this.name = name;
    }

    public void addCallback(ImageSaveCallback callback){
        this.callback = callback;
    }

    @Override
    public void run() {
        //Looper.prepare();

        ThreadUtil.start();
        Context context = AppGlobals.getInstance().getApplicationContext();
        try {
            String imageDir = AppGlobals.getInstance().getImagePath();
            String filePath = BitmapUtil.saveToFile(bitmap, imageDir,name);
            MediaStoreUtil.addImageToGallery(context,new File(filePath));
            //String path = MediaStoreUtil.addImageToGallery(context, bitmap, name);
            Log.d(TAG,"saving image takes " + ThreadUtil.getElapse() + "ms");
            if(callback != null){
                callback.onImageSaveDone(imageDir);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if(callback != null){
                callback.onImageSaveDone(null);
            }
        }
        //Looper.loop();
    }
}
