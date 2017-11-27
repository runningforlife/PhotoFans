package com.github.runningforlife.photosniffer.presenter;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
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

public class WallpaperPresenterImpl implements WallpaperPresenter{
    private static final String TAG = "WallpaperPresenter";
    private Context mContext;
    private WallpaperView mView;
    private RealmManager mRealmMgr;
    private RealmResults<ImageRealm> mWallpaper;
    private ExecutorService mExecutor;
    private UpdateOp mOp;
    private int mLastRemovedPos = -1;

    public WallpaperPresenterImpl(Context context){
        mContext = context;
        mRealmMgr = RealmManager.getInstance();
        mExecutor = Executors.newSingleThreadExecutor();
        mOp = UpdateOp.OP_DELETE;
    }

    @Override
    public void setView(WallpaperView view){
        mView = view;
    }

    @Override
    public void init() {
        Log.v(TAG,"init()");
        mRealmMgr.onStart();
    }

    @Override
    public int getItemCount() {
        return mWallpaper.size();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        return mWallpaper.get(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        if(pos >= 0 && pos <= mWallpaper.size()) {
            mRealmMgr.delete(mWallpaper.get(pos));
            mLastRemovedPos = pos;
            mOp = UpdateOp.OP_DELETE;
        }
    }

    @Override
    public void saveImageAtPos(final int pos) {
        if (pos >= 0 && pos < mWallpaper.size()) {
            mOp = UpdateOp.OP_MODIFY;
            GlideLoaderListener listener = new GlideLoaderListener(null);
            listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
                @Override
                public void onImageLoadDone(Object o) {
                    Log.d(TAG, "onImageLoadDone()");
                    if(o instanceof Bitmap) {
                        ImageSaveRunnable r = new ImageSaveRunnable(((Bitmap) o), mWallpaper.get(pos).getName());
                        r.addCallback(WallpaperPresenterImpl.this);
                        mExecutor.submit(r);
                    }
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
        Log.v(TAG,"onWallpaperDataChange()");

        if(mWallpaper == null){
            mWallpaper = data;
            mView.onDataSetRangeChange(0, mWallpaper.size());
        }else if(mOp == UpdateOp.OP_DELETE){
            mView.onDataSetRangeChange(mLastRemovedPos, -1);
        }else {
            // nothing happened
            mView.onDataSetRangeChange(0, 0);
        }

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
        mRealmMgr.addWallpaperDataChangeListener(this);
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
                    WallpaperManager wpm = WallpaperManager.getInstance(mContext);
                    try {
                        if(Build.VERSION.SDK_INT >= 24) {
                            wpm.setBitmap((Bitmap) o, null, false, WallpaperManager.FLAG_LOCK | WallpaperManager.FLAG_SYSTEM);
                        }else{
                            wpm.setBitmap((Bitmap)o);
                        }
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
