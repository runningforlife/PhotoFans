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

public class FavorImagePresenterImpl extends PresenterBase {
    private static final String TAG = "FavorImagePresenter";
    private ExecutorService mExecutor;

    public FavorImagePresenterImpl(Context context, FavorPictureView view){
        super(context, view);
        mExecutor = Executors.newSingleThreadExecutor();
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
            mRealmApi.deleteSync(mImageList.get(pos));
        }
    }

    @Override
    public void init() {
        Log.v(TAG,"init()");
        // start loading data
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
    }

    @Override
    public void onDestroy() {
        Log.v(TAG,"onDestroy()");
        //mRealmMgr.onDestroy();
    }
}
