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

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jason on 4/6/17.
 */

public class ImageDetailPresenterImpl extends PresenterBase implements ImageDetailPresenter {
    private static final String TAG = "ImageDetailPresenter";

    private Context mContext;
    //private ImageDetailView mView;
    private ExecutorService mExecutor;


    public ImageDetailPresenterImpl(Context context, ImageDetailView view){
        super(view);
        mContext = context;
        //mView = view;
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
            mRealmMgr.delete(mImageList.get(pos));
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
                    ((ImageDetailView)mView).onImageSaveDone(null);
                }
            }
        });
        GlideLoader.downloadOnly(mContext, mImageList.get(pos).getUrl(), listener,
                Priority.HIGH,Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT, false);
    }

    @Override
    public void favorImageAtPos(int pos) {
        Log.d(TAG,"favorImageAtPos(): pos = " + pos);
        Realm r = Realm.getDefaultInstance();
        r.beginTransaction();

        ImageRealm favor = mImageList.get(pos);
        favor.setIsFavor(true);

        r.commitTransaction();
    }

    @Override
    public void setWallpaperAtPos(final int pos) {
        Log.v(TAG,"setWallpaperAtPos(): pos = " + pos);
        GlideLoaderListener listener = new GlideLoaderListener(null);
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.d(TAG,"onImageLoadDone()");
                if(o instanceof Bitmap) {
                    WallpaperManager wpm =  WallpaperManager.getInstance(mContext);
                    try {
                        wpm.setBitmap((Bitmap)o);
                        ((ImageDetailView)mView).onWallpaperSetDone(true);

                        markAsWallpaper(mImageList.get(pos).getUrl());
                        //mRealmMgr.setWallpaper(mImageList.get(pos).getUrl());
                    } catch (IOException e) {
                        ((ImageDetailView)mView).onWallpaperSetDone(false);
                        e.printStackTrace();
                    }
                }else{
                    ((ImageDetailView)mView).onWallpaperSetDone(false);
                }
            }
        });
        GlideLoader.downloadOnly(mContext, mImageList.get(pos).getUrl(), listener,
                Priority.HIGH, dm.widthPixels, dm.heightPixels, true);
    }

    @Override
    public void init() {
        Log.v(TAG,"init()");
        mRealmMgr.onStart();
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
        //mRealmMgr.addUsedDataChangeListener(this);
    }

    @Override
    public void onDestroy() {
        //mRealmMgr.removeUsedDataChangeListener(this);
        mRealmMgr.onDestroy();
    }


    @Override
    public void onImageSaveDone(String path) {
        Log.d(TAG,"onImageSaveDone()");
        ((ImageDetailView)mView).onImageSaveDone(path);
    }
}
