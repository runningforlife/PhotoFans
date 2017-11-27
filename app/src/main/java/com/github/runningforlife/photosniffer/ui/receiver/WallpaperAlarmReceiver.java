package com.github.runningforlife.photosniffer.ui.receiver;

import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.R;
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
        String action = intent.getAction();

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        boolean isAutoWallpaper = sp.getBoolean(context.getString(R.string.pref_automatic_wallpaper), true);
        boolean isAutoLockScreen = sp.getBoolean(context.getString(R.string.pref_enable_auto_lockscreen_wallpaper), false);

        if(ALARM_AUTO_WALLPAPER.equals(action) && isAutoWallpaper){
            MyThreadFactory.getInstance().
                    newThread(new Runnable() {
                @Override
                public void run() {
                    if(Build.VERSION.SDK_INT >= 24) {
                        setWallpaper(WallpaperManager.FLAG_SYSTEM);
                    }else{
                        setWallpaper(-1);
                    }
                }
            }).start();
        }else if(Intent.ACTION_SCREEN_ON.equals(action) && isAutoLockScreen){
            MyThreadFactory.getInstance().newThread(new Runnable() {
                        @Override
                        public void run() {
                            if(Build.VERSION.SDK_INT >= 24) {
                                setWallpaper(WallpaperManager.FLAG_LOCK);
                            }
                        }
                    });
        }
    }

    private void setWallpaper(final int flag){
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
                        WallpaperManager wpm = WallpaperManager.getInstance(context);
                        try {
                            if(Build.VERSION.SDK_INT >= 24 && flag != -1) {
                                wpm.setBitmap((Bitmap) o, null, true, flag);
                            }else{
                                wpm.setBitmap((Bitmap)o);
                            }
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
                GlideLoader.downloadOnly(context, newUrl, listener, Priority.IMMEDIATE,
                        Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_WIDTH);
            }else {
                GlideLoader.downloadOnly(context, imgUrl, listener, Priority.IMMEDIATE,
                        Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_WIDTH);
            }
        }finally {
            rm.close();
        }
    }
}
