package com.github.runningforlife.photosniffer.presenter;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.data.local.RealmManager;
import com.github.runningforlife.photosniffer.ui.WallpaperView;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.RealmResults;

/**
 * wallpaper presenter to get UI data
 */

public class WallpaperPresenterImpl extends PresenterBase {
    private static final String TAG = "WallpaperPresenter";
    private ExecutorService mExecutor;

    public WallpaperPresenterImpl(Context context, WallpaperView view){
        super(context, view);
        mContext = context;
        mExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void init() {
        Log.v(TAG,"init()");
        //mRealmMgr.onStart();
    }

    @Override
    public int getItemCount() {
        return mImageList.size();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        return mImageList.get(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        if (pos >= 0 && pos < mImageList.size()) {
            mRealmApi.deleteSync(mImageList.get(pos));
        }
    }

    @Override
    public void saveImageAtPos(final int pos) {
        if (pos >= 0 && pos < mImageList.size()) {
            GlideLoaderListener listener = new GlideLoaderListener(null);
            listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
                @Override
                public void onImageLoadDone(Object o) {
                    Log.d(TAG, "onImageLoadDone()");
                    if(o instanceof Bitmap) {
                        ImageSaveRunnable r = new ImageSaveRunnable(((Bitmap) o), mImageList.get(pos).getName());
                        r.addCallback(WallpaperPresenterImpl.this);
                        mExecutor.submit(r);
                    }
                }
            });

            String imgUrl = mImageList.get(pos).getUrl();
            GlideLoader.downloadOnly(mContext, imgUrl, listener,Priority.HIGH,
                    Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT, false);
        }
    }

    @Override
    public void onImageSaveDone(String path) {
        Log.v(TAG,"onImageSaveDone()");
        ((WallpaperView)mView).onImageSaveDone(path);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onStart() {
        Log.v(TAG,"onStart()");

        HashMap<String,String> params = new HashMap<>();
        params.put("mIsUsed", Boolean.toString(true));
        params.put("mIsFavor", Boolean.toString(false));
        params.put("mIsWallpaper", Boolean.toString(true));
        mImageList = (RealmResults<ImageRealm>) mRealmApi.queryAsync(ImageRealm.class, params);
        mImageList.addChangeListener(mOrderRealmChangeListener);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG,"onDestroy()");
        //mRealmMgr.onDestroy();
    }

    @Override
    public void setWallpaperAtPos(int pos) {
        if(pos < 0) return;

        String url = mImageList.get(pos).getUrl();

        setWallpaper(url);
    }
}
