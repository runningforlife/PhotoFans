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
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.data.local.RealmManager;
import com.github.runningforlife.photosniffer.ui.FavorPictureView;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.RealmResults;

/**
 * presenter to get favor image list
 */

public class FavorImagePresenterImpl extends PresenterBase implements FavorImagePresenter {
    private static final String TAG = "FavorImagePresenter";
    private Context mContext;
    private ExecutorService mExecutor;
    private boolean mIsRefreshing;
    private int mLastRemovedPos = -1;

    public FavorImagePresenterImpl(Context context, FavorPictureView view){
        super(view);
        mContext = context;
        mExecutor = Executors.newSingleThreadExecutor();
        mIsRefreshing = false;
    }

    @Override
    public int getItemCount() {
        if(mImageList == null) return 0;

        return mImageList.size();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        if(mImageList == null) return null;

        return mImageList.get(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        if(pos >= 0 && pos <= mImageList.size()) {
            mRealmMgr.delete(mImageList.get(pos));
            mLastRemovedPos = pos;
        }
    }

    @Override
    public void saveImageAtPos(final int pos) {
        if(pos < 0 || pos >= mImageList.size()) return;

        GlideLoaderListener listener = new GlideLoaderListener(null);
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.d(TAG,"onImageLoadDone()");
                if(o instanceof Bitmap) {
                    ImageSaveRunnable r = new ImageSaveRunnable(((Bitmap) o), mImageList.get(pos).getName());
                    r.addCallback(FavorImagePresenterImpl.this);
                    mExecutor.submit(r);
                }
            }
        });
        GlideLoader.downloadOnly(mContext, mImageList.get(pos).getUrl(), listener,
                Priority.HIGH,DEFAULT_WIDTH, DEFAULT_HEIGHT, false);
    }

    @Override
    public void refresh() {
        Log.v(TAG,"refresh()");
        mIsRefreshing = true;
        //mRealmMgr.queryAllAsync();
        //mRealmMgr.addFavorDataChangeListener(this);

    }

    @Override
    public void setWallpaperAtPos(int pos) {
        Log.v(TAG,"setWallpaperAtPos()");
        if(pos >= 0 && pos < mImageList.size()) {
            setWallpaper(mImageList.get(pos).getUrl());
        }
    }

    @Override
    public void init() {
        Log.v(TAG,"init()");
        // start loading data
        mRealmMgr.onStart();
        //mRealmMgr.addFavorDataChangeListener(this);
    }

    @Override
    public void onImageSaveDone(String path) {
        Log.v(TAG,"onImageSaveDone()");
        ((FavorPictureView)mView).onImageSaveDone(path);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onStart() {
        Log.v(TAG,"onStart()");

        HashMap<String,String> params = new HashMap<>();
        params.put("mIsUsed", Boolean.toString(true));
        params.put("mIsFavor", Boolean.toString(true));
        params.put("mIsWallpaper", Boolean.toString(false));
        mImageList = (RealmResults<ImageRealm>) mRealmApi.queryAsync(ImageRealm.class, params);
        mImageList.addChangeListener(mOrderRealmChangeListener);
        //mRealmMgr.addFavorDataChangeListener(this);
    }

    @Override
    public void onDestroy() {
        mRealmMgr.onDestroy();
        //mRealmMgr.removeFavorDataChangeListener(this);
    }

    private void setWallpaper(String url){
        if(TextUtils.isEmpty(url)) return;

        GlideLoaderListener listener = new GlideLoaderListener(null);
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.d(TAG,"onImageLoadDone()");
                if(o instanceof Bitmap) {
                    WallpaperManager wpm = WallpaperManager.getInstance(mContext);
                    try {
                        wpm.setBitmap((Bitmap)o);
                        ((FavorPictureView)mView).onWallpaperSetDone(true);
                    } catch (IOException e) {
                        ((FavorPictureView)mView).onWallpaperSetDone(false);
                        e.printStackTrace();
                    }
                }else{
                    ((FavorPictureView)mView).onWallpaperSetDone(false);
                }
            }
        });
        GlideLoader.downloadOnly(mContext, url, listener, Priority.HIGH,
                dm.widthPixels, dm.heightPixels, true);

        markAsWallpaper(url);
    }
}
