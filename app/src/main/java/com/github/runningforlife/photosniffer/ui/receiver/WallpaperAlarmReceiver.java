package com.github.runningforlife.photosniffer.ui.receiver;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.github.runningforlife.photosniffer.app.AppGlobals;
import com.github.runningforlife.photosniffer.crawler.processor.ImageSource;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.service.MyThreadFactory;

import java.io.IOException;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * auto wallpaper alarm receiver
 */

public class WallpaperAlarmReceiver extends BroadcastReceiver{
    private static final String TAG = "WallpaperAlarmReceiver";

    public static final String ALARM_AUTO_WALLPAPER = "com.github.runningforlife.AUTO_WALLPAPER";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG,"onReceive()");
        if(ALARM_AUTO_WALLPAPER.equals(intent.getAction())){
            MyThreadFactory.getInstance().
                    newThread(new Runnable() {
                @Override
                public void run() {
                    setWallpaper();
                }
            }).start();
        }
    }

    private void setWallpaper(){
        Log.v(TAG,"setWallpaper()");
        Realm rm = Realm.getDefaultInstance();
        try {
            RealmResults<ImageRealm> wallpaper = rm.where(ImageRealm.class)
                    .equalTo("mIsWallpaper", true)
                    .findAll();

            if (wallpaper.size() <= 0) return;

            final Context context = AppGlobals.getInstance();

            Random rnd = new Random();
            final int pos = rnd.nextInt(wallpaper.size());

            GlideLoaderListener listener = new GlideLoaderListener(null);
            listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
                @Override
                public void onImageLoadDone(Object o) {
                    Log.d(TAG, "onImageLoadDone()");
                    if (o instanceof Bitmap) {
                        WallpaperManager wpm = (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
                        try {
                            wpm.setBitmap((Bitmap) o);
                        } catch (IOException e) {
                            //mView.onWallpaperSetDone(false);
                            e.printStackTrace();
                        }
                    }
                }
            });
            String imgUrl = wallpaper.get(pos).getUrl();
            if(imgUrl.endsWith(ImageSource.POLA_IMAGE_END)){
                final String newUrl = imgUrl.substring(0, imgUrl.lastIndexOf("/")+1) +
                        ImageSource.POLA_FULL_IMAGE_END;
                GlideLoader.downloadOnly(context, newUrl, listener,
                        Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_WIDTH);
            }else {
                GlideLoader.downloadOnly(context, imgUrl, listener,
                        Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_WIDTH);
            }
        }finally {
            rm.close();
        }
    }
}
