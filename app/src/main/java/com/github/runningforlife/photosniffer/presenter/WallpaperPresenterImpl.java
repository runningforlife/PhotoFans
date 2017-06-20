package com.github.runningforlife.photosniffer.presenter;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.ui.WallpaperView;

import java.io.IOException;
import java.util.Random;

import io.realm.RealmResults;

/**
 * wallpaper presenter to get UI data
 */

public class WallpaperPresenterImpl extends WallpaperPresenter{
    private static final String TAG = "WallpaperPresenter";
    private Context mContext;
    private WallpaperView mView;
    private RealmManager mRealmMgr;
    private RealmResults<ImageRealm> mWallpaper;

    public WallpaperPresenterImpl(Context context, WallpaperView view){
        mContext = context;
        mView = view;
        mRealmMgr = RealmManager.getInstance();
    }

    @Override
    public void init() {
        Log.v(TAG,"init()");
        mRealmMgr.onStart();
    }

    @Override
    public int getItemCount() {
        checkNotNull(mWallpaper);
        return mWallpaper.size();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        checkNotNull(mWallpaper);
        return mWallpaper.get(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        checkNotNull(mWallpaper);
        mRealmMgr.delete(mWallpaper.get(pos));
    }

    @Override
    public void saveImageAtPos(int pos) {

    }

    @Override
    public void onWallpaperDataChange(RealmResults<ImageRealm> data) {
        if(data == null){
            throw new NullPointerException("data set is null");
        }

        mWallpaper = data;

        mView.onDataSetChanged();
    }

    @Override
    public void onImageSaveDone(String path) {

    }

    @Override
    public void onStart() {
        Log.v(TAG,"onStart()");
        mRealmMgr.addListener(this);
    }

    @Override
    public void onDestroy() {
        mRealmMgr.onDestroy();
    }

    @Override
    public void refresh() {
        mRealmMgr.queryAllAsync();
    }

    @Override
    public void setWallpaper() {
        Log.v(TAG,"setWallpaper()");
        // random to choose a picture from wallpaper data
        if(mWallpaper == null || mWallpaper.size() <= 0) return;

        Random rnd = new Random();
        final int pos = rnd.nextInt(mWallpaper.size());

        GlideLoaderListener listener = new GlideLoaderListener(null);
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.d(TAG,"onImageLoadDone()");
                if(o instanceof Bitmap) {
                    WallpaperManager wpm = (WallpaperManager)mContext.getSystemService(Context.WALLPAPER_SERVICE);
                    try {
                        wpm.setBitmap((Bitmap)o);
                        //mView.onWallpaperSetDone(true);
                        // mark it as wall paper
                        //mRealmMgr.setWallpaper(mWallpaper.get(pos).getUrl());
                    } catch (IOException e) {
                        //mView.onWallpaperSetDone(false);
                        e.printStackTrace();
                    }
                }else{
                    //mView.onWallpaperSetDone(false);
                }
            }
        });
        GlideLoader.downloadOnly(mContext, mWallpaper.get(pos).getUrl(), listener,
                Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT);
    }

}
