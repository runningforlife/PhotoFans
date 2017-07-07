package com.github.runningforlife.photosniffer.presenter;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.crawler.processor.ImageSource;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.ui.WallpaperView;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ExecutorService mExecutor;


    public WallpaperPresenterImpl(Context context){
        mContext = context;
        mRealmMgr = RealmManager.getInstance();
        mExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void setView(WallpaperView view){
        mView = view;
    }

    @Override
    public void init() {
        Log.v(TAG,"init()");
        mRealmMgr.addWallpaperDataChangeListener(this);
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
        if(pos >= 0 && pos <= mWallpaper.size()) {
            mRealmMgr.delete(mWallpaper.get(pos));
        }
    }

    @Override
    public void saveImageAtPos(final int pos) {
        if (pos >= 0 && pos < mWallpaper.size()) {
            GlideLoaderListener listener = new GlideLoaderListener(null);
            listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
                @Override
                public void onImageLoadDone(Object o) {
                    Log.d(TAG, "onImageLoadDone()");
                    ImageSaveRunnable r = new ImageSaveRunnable(((Bitmap) o), mWallpaper.get(pos).getName());
                    r.addCallback(WallpaperPresenterImpl.this);
                    mExecutor.submit(r);
                }
            });

            String imgUrl = mWallpaper.get(pos).getUrl();
            if (imgUrl.endsWith(ImageSource.POLA_IMAGE_END)) {
                final String newUrl = imgUrl.substring(0, imgUrl.lastIndexOf("/") + 1) +
                        ImageSource.POLA_FULL_IMAGE_END;
                GlideLoader.downloadOnly(mContext, newUrl, listener, Priority.HIGH,
                        Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT);
            } else {
                GlideLoader.downloadOnly(mContext, imgUrl, listener,Priority.HIGH,
                        Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT);
            }
        }
    }

    @Override
    public void onWallpaperDataChange(RealmResults<ImageRealm> data) {
        if(data == null){
            throw new NullPointerException("data set is null");
        }

        mWallpaper = data;

        mView.onDataSetChanged();

        mView.onRefreshDone(true);
    }

    @Override
    public void onImageSaveDone(String path) {
        Log.v(TAG,"onImageSaveDone()");
        mView.onImageSaveDone(path);
    }

    @Override
    public void onStart() {
        Log.v(TAG,"onStart()");
        mRealmMgr.onStart();
    }

    @Override
    public void onDestroy() {
        mRealmMgr.onDestroy();

        mRealmMgr.removeWallpaperDataChangeListener(this);
    }

    @Override
    public void refresh() {
        mRealmMgr.queryAllAsync();
    }

    @Override
    public void setWallpaperAtPos(int pos) {
        if(pos < 0) return;

        String url = mWallpaper.get(pos).getUrl();

        setWallpaper(url);
    }

    private void setWallpaper(String url){
        if(TextUtils.isEmpty(url)) return;

        GlideLoaderListener listener = new GlideLoaderListener(null);
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.d(TAG,"onImageLoadDone()");
                if(o instanceof Bitmap) {
                    WallpaperManager wpm = (WallpaperManager)mContext.getSystemService(Context.WALLPAPER_SERVICE);
                    try {
                        // TODO: use flag to distinguish system and lock screen wallpaper
                        wpm.setBitmap((Bitmap)o);
                        mView.onWallpaperSetDone(true);
                    } catch (IOException e) {
                        mView.onWallpaperSetDone(false);
                        e.printStackTrace();
                    }
                }else{
                    mView.onWallpaperSetDone(false);
                }
            }
        });
        GlideLoader.downloadOnly(mContext, url, listener, Priority.HIGH,
                Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT);
    }
}
