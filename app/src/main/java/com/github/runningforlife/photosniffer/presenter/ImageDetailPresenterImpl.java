package com.github.runningforlife.photosniffer.presenter;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.data.local.RealmManager;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.ui.ImageDetailView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jason on 4/6/17.
 */

public class ImageDetailPresenterImpl implements ImageDetailPresenter {
    private static final String TAG = "ImageDetailPresenter";

    private Context mContext;
    private RealmResults<ImageRealm> mImgList;
    private ImageDetailView mView;
    private RealmManager mRealmMgr;
    private ExecutorService mExecutor;
    private UpdateOp mOp;
    private int mLastRemovePos;


    public ImageDetailPresenterImpl(Context context, ImageDetailView view){
        mContext = context;
        mView = view;
        mRealmMgr = RealmManager.getInstance();
        mExecutor = Executors.newSingleThreadExecutor();

        mOp = UpdateOp.OP_NONE;
        mLastRemovePos = -1;
        //mImgList = new SortedList<ImageRealm>(ImageRealm.class, new SortedListCallback());
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        return mImgList.get(pos);
    }

    @Override
    public int getItemCount() {
        if(mImgList == null) return 0;

        return mImgList.size();
    }

    @Override
    public void removeItemAtPos(int pos) {
        Log.d(TAG,"removeItemAtPos()");
        if(pos >= 0 && pos < mImgList.size()) {
            mRealmMgr.delete(mImgList.get(pos));
        }
        mOp = UpdateOp.OP_DELETE;
        mLastRemovePos = pos;
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
                    ImageSaveRunnable r = new ImageSaveRunnable(((Bitmap) o), mImgList.get(pos).getName());
                    r.addCallback(ImageDetailPresenterImpl.this);
                    mExecutor.submit(r);
                }else{
                    mView.onImageSaveDone(null);
                }
            }
        });
        GlideLoader.downloadOnly(mContext, mImgList.get(pos).getUrl(), listener,
                Priority.HIGH,Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT, false);
    }

    @Override
    public void favorImageAtPos(int pos) {
        Log.d(TAG,"favorImageAtPos(): pos = " + pos);
        Realm r = Realm.getDefaultInstance();
        r.beginTransaction();

        ImageRealm favor = mImgList.get(pos);
        favor.setIsFavor(true);

        r.commitTransaction();
    }

    @Override
    public void setWallpaper(final int pos) {
        Log.v(TAG,"setWallpaper(): pos = " + pos);
        mOp = UpdateOp.OP_MODIFY;
        GlideLoaderListener listener = new GlideLoaderListener(null);
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.d(TAG,"onImageLoadDone()");
                if(o instanceof Bitmap) {
                    WallpaperManager wpm =  WallpaperManager.getInstance(mContext);
                    try {
                        if(Build.VERSION.SDK_INT >= 24) {
                            wpm.setBitmap((Bitmap) o, null, false, WallpaperManager.FLAG_LOCK | WallpaperManager.FLAG_SYSTEM);
                        }else{
                            wpm.setBitmap((Bitmap)o);
                        }
                        mView.onWallpaperSetDone(true);
                        // mark it as wall paper
                        mRealmMgr.setWallpaper(mImgList.get(pos).getUrl());
                    } catch (IOException e) {
                        mView.onWallpaperSetDone(false);
                        e.printStackTrace();
                    }
                }else{
                    mView.onWallpaperSetDone(false);
                }
            }
        });
        GlideLoader.downloadOnly(mContext, mImgList.get(pos).getUrl(), listener,
                Priority.HIGH, dm.widthPixels, dm.heightPixels, true);
    }

    @Override
    public void init() {
        Log.v(TAG,"init()");
        mRealmMgr.onStart();
    }

    @Override
    public void onStart() {
        Log.v(TAG,"onStart()");
        mRealmMgr.addUsedDataChangeListener(this);
    }

    @Override
    public void onDestroy() {
        mRealmMgr.removeUsedDataChangeListener(this);
        mRealmMgr.onDestroy();
    }

    @Override
    public void onUsedDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG,"onUsedDataChange(): data size = " + data.size());

        data.sort("mTimeStamp", Sort.DESCENDING);

        if(mImgList == null){
            mImgList = data;
            mView.onDataSetRangeChange(0, mImgList.size());
        }else if(mOp == UpdateOp.OP_DELETE){
            mView.onDataSetRangeChange(mLastRemovePos, -1);
        }else if(mOp == UpdateOp.OP_MODIFY){
            mView.onDataSetRangeChange(0,0);
        }
    }

    @Override
    public void onImageSaveDone(String path) {
        Log.d(TAG,"onImageSaveDone()");
        mView.onImageSaveDone(path);
    }
}
