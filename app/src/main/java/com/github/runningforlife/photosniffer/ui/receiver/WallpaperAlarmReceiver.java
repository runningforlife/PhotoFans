package com.github.runningforlife.photosniffer.ui.receiver;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.app.AppGlobals;
import com.github.runningforlife.photosniffer.crawler.processor.ImageSource;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.service.LockScreenUpdateService;
import com.github.runningforlife.photosniffer.service.MyThreadFactory;
import com.github.runningforlife.photosniffer.utils.BitmapUtil;
import com.github.runningforlife.photosniffer.utils.DisplayUtil;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * auto wallpaper alarm receiver
 */

public class WallpaperAlarmReceiver extends BroadcastReceiver{
    private static final String TAG = "WallpaperAlarmReceiver";

    public static final String ALARM_AUTO_WALLPAPER = "com.github.runningforlife.AUTO_WALLPAPER";

    private static AtomicInteger sWallpaperCount = new AtomicInteger(0);
    private static final DisplayMetrics dm = DisplayUtil.getScreenDimen();


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.v(TAG,"onReceive(): action=" + action);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        boolean isAutoWallpaper = sp.getBoolean(context.getString(R.string.pref_automatic_wallpaper), true);

        if(ALARM_AUTO_WALLPAPER.equals(action) && isAutoWallpaper){
            MyThreadFactory.getInstance().newThread(new Runnable() {
                @Override
                public void run() {
                    setWallpaper(-1);
                }
            }).start();
        }else if(action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)){
            if (Build.VERSION.SDK_INT >= 24) {
                // start wallpaper service
                Intent intent1 = new Intent(context, LockScreenUpdateService.class);
                context.startService(intent1);
            }
        }
    }

    private void setWallpaper(final int flag){
        Log.v(TAG,"setWallpaperAtPos()");
        Realm rm = Realm.getDefaultInstance();
        try {
            RealmQuery<ImageRealm> query = rm.where(ImageRealm.class);
            RealmResults<ImageRealm> wallpaper = query
                    .equalTo("mIsWallpaper", true)
                    .or()
                    .equalTo("mIsFavor", true)
                    .findAll();
            if(wallpaper.size() <= 0){
                wallpaper = query
                        .or()
                        .equalTo("mIsUsed", true)
                        .findAll();
            }

            if (wallpaper.size() <= 0) return;

            final Context context = AppGlobals.getInstance();

            final int pos = sWallpaperCount.getAndIncrement()%wallpaper.size();

            GlideLoaderListener listener = new GlideLoaderListener(null);
            listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
                @Override
                public void onImageLoadDone(Object o) {
                    Log.d(TAG, "onImageLoadDone()");
                    if (o instanceof Bitmap) {
                        // check bitmap size
                        Bitmap bm = (Bitmap)o;
                        WallpaperManager wpm = WallpaperManager.getInstance(context);
                        try {
                            wpm.setBitmap(bm);
                        } catch (IOException e) {
                            //mView.onWallpaperSetDone(false);
                            e.printStackTrace();
                        }
                    }
                }
            });

            String imgUrl = wallpaper.get(pos).getUrl();
            GlideLoader.downloadOnly(context, imgUrl, listener, Priority.IMMEDIATE,
                    dm.widthPixels, dm.heightPixels, true);
        } finally {
            rm.close();
        }
    }
}
