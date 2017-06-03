package com.github.runningforlife.photofans.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.util.Log;

import com.github.runningforlife.photofans.app.AppGlobals;
import com.github.runningforlife.photofans.loader.GlideLoader;
import com.github.runningforlife.photofans.loader.GlideLoaderListener;
import com.github.runningforlife.photofans.model.ImageRealm;
import com.github.runningforlife.photofans.utils.BitmapUtil;
import com.github.runningforlife.photofans.utils.DisplayUtil;
import com.github.runningforlife.photofans.utils.MediaStoreUtil;
import com.github.runningforlife.photofans.utils.ThreadUtil;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * a runnable to save image
 */

public class ImageSaveRunnable implements Runnable{
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
            String filePath = BitmapUtil.saveToFile(bitmap,AppGlobals.getInstance().getImagePath(),name);
            MediaStoreUtil.addImageToGallery(context,new File(filePath));
            //String path = MediaStoreUtil.addImageToGallery(context, bitmap, name);
            Log.d(TAG,"saving image takes " + ThreadUtil.getElapse() + "ms");
            if(callback != null){
                callback.onImageSaveDone(filePath);
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
