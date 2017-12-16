package com.github.runningforlife.photosniffer.presenter;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;

import io.realm.Realm;
import io.realm.RealmResults;

import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.ui.ImageDetailView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jason on 4/6/17.
 */

public class ImageDetailPresenterImpl extends PresenterBase implements ImageDetailPresenter {
    private static final String TAG = "ImageDetailPresenter";
    private ImageDetailView mView;
    private ExecutorService mExecutor;

    public ImageDetailPresenterImpl(Context context, ImageDetailView view){
        super(context, view);
        mContext = context;
        mView = view;
        mExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        return mImageList.get(pos);
    }

    @Override
    public int getItemCount() {
        if(mImageList == null) return 0;

        return mImageList.size();
    }

    @Override
    public void removeItemAtPos(int pos) {
        Log.d(TAG,"removeItemAtPos()");
        if(pos >= 0 && pos < mImageList.size()) {
            mRealmApi.deleteSync(mImageList.get(pos));
        }
    }

    @Override
    public void saveImageAtPos(final int pos) {
        Log.v(TAG,"saveImageAtPos(): pos " + pos);

        GlideLoaderListener listener = new GlideLoaderListener(null);
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.d(TAG,"onImageLoadDone()");
                if(o instanceof Bitmap) {
                    ImageSaveRunnable r = new ImageSaveRunnable(((Bitmap) o), mImageList.get(pos).getName());
                    r.addCallback(ImageDetailPresenterImpl.this);
                    mExecutor.submit(r);
                }else{
                    mView.onImageSaveDone(null);
                }
            }
        });
        GlideLoader.downloadOnly(mContext, mImageList.get(pos).getUrl(), listener,
                Priority.HIGH,Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT, false);
    }

    @Override
    public void favorImageAtPos(int pos) {
        Log.d(TAG,"favorImageAtPos(): pos = " + pos);

        markAsFavor(mImageList.get(pos).getUrl());
    }

    @Override
    public void setWallpaperAtPos(final int pos) {
        Log.v(TAG,"setWallpaperAtPos(): pos = " + pos);

        setWallpaper(mImageList.get(pos).getUrl());
    }

    @Override
    public void init() {
        Log.v(TAG,"init()");
        //mRealmMgr.onStart();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onStart() {
        Log.v(TAG,"onStart()");
        HashMap<String,String> params = new HashMap<>();
        params.put("mIsUsed", Boolean.toString(true));
        params.put("mIsFavor", Boolean.toString(false));
        params.put("mIsWallpaper", Boolean.toString(false));
        mImageList = (RealmResults<ImageRealm>) mRealmApi.queryAsync(ImageRealm.class, params);
        mImageList.addChangeListener(mOrderRealmChangeListener);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG,"onDestroy()");
        //mRealmMgr.onDestroy();
    }


    @Override
    public void onImageSaveDone(String path) {
        Log.d(TAG,"onImageSaveDone()");
        mView.onImageSaveDone(path);
    }

    @Override
    public void onImageLoadStart(int pos) {
        Log.v(TAG,"onImageLoadStart()");
        mView.onImageLoadStart(pos);
    }

    @Override
    public void onImageLoadDone(int pos, boolean isSuccess) {
        Log.v(TAG,"onImageLoadDone()");
        mView.onImageLoadDone(pos, isSuccess);
    }

}
