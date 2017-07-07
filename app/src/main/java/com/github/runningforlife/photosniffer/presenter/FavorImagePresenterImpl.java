package com.github.runningforlife.photosniffer.presenter;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.ui.FavorView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * presenter to get favor image list
 */

public class FavorImagePresenterImpl extends FavorImagePresenter{
    private static final String TAG = "FavorImagePresenter";
    private FavorView mView;
    private Context mContext;
    private RealmManager mRealmMgr;
    private RealmResults<ImageRealm> mFavorList;
    private ExecutorService mExecutor;
    private boolean mIsRefreshing;

    public FavorImagePresenterImpl(Context context, FavorView view){
        mContext = context;
        mView = view;
        mRealmMgr = RealmManager.getInstance();
        mExecutor = Executors.newSingleThreadExecutor();
        mIsRefreshing = false;
    }

    @Override
    public int getItemCount() {
        if(mFavorList == null) return 0;

        return mFavorList.size();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        if(mFavorList == null) return null;

        return mFavorList.get(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        if(pos >= 0 && pos <= mFavorList.size()) {
            mRealmMgr.delete(mFavorList.get(pos));
        }
    }

    @Override
    public void saveImageAtPos(final int pos) {
        if(pos < 0 || pos >= mFavorList.size()) return;

        GlideLoaderListener listener = new GlideLoaderListener(null);
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.d(TAG,"onImageLoadDone()");
                ImageSaveRunnable r = new ImageSaveRunnable(((Bitmap)o), mFavorList.get(pos).getName());
                r.addCallback(FavorImagePresenterImpl.this);
                mExecutor.submit(r);
            }
        });
        GlideLoader.downloadOnly(mContext, mFavorList.get(pos).getUrl(), listener,
                Priority.HIGH,DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public void refresh() {
        Log.v(TAG,"refresh()");
        mIsRefreshing = true;
        mRealmMgr.queryAllAsync();
        mRealmMgr.addFavorDataChangeListener(this);

    }

    @Override
    public void cancelFavorAtPos(int pos) {
        Log.v(TAG,"cancelFavorAtPos()");
        Realm r =  Realm.getDefaultInstance();
        r.beginTransaction();

        ImageRealm img = mFavorList.get(pos);
        img.setIsFavor(false);

        r.commitTransaction();
    }

    @Override
    public void setWallpaperAtPos(int pos) {
        Log.v(TAG,"setWallpaperAtPos()");
        if(pos >= 0 && pos < mFavorList.size()) {
            setWallpaper(mFavorList.get(pos).getUrl());
        }
    }

    @Override
    public void init() {
        Log.v(TAG,"init()");
        mRealmMgr.addFavorDataChangeListener(this);
    }

    @Override
    public void onFavorDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG,"onFavorDataChange(): data size = " + data.size());
        mFavorList = data;

        if(mView != null) {
            mView.onDataSetChanged();
        }

        if(mIsRefreshing && mView != null){
            mView.onRefreshDone(true);
            mIsRefreshing = false;
        }
    }

    @Override
    public void onImageSaveDone(String path) {
        Log.v(TAG,"onImageSaveDone()");
        mView.onImageSaveDone(path);
    }

    @Override
    public void onStart() {
        Log.v(TAG,"onStart()");
        // start loading data
        mRealmMgr.onStart();
    }

    @Override
    public void onDestroy() {
        mRealmMgr.onDestroy();
        mRealmMgr.removeFavorDataChangeListener(this);
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
