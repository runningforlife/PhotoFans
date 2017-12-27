package com.github.runningforlife.photosniffer.presenter;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
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

import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_FAVOR;
import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_GALLERY;
import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_WALLPAPER;

/**
 * Created by jason on 4/6/17.
 */

public class ImageDetailPresenterImpl extends PresenterBase implements ImageDetailPresenter {
    private static final String TAG = "ImageDetailPresenter";
    private ImageDetailView mView;
    private @ImageType int mImageType;

    public ImageDetailPresenterImpl(RequestManager requestManager, Context context, ImageDetailView view, @ImageType int imageType){
        super(requestManager,context, view);
        mContext = context;
        mView = view;
        mImageType = imageType;
    }

    @Override
    public void favorImageAtPos(int pos) {
        Log.d(TAG,"favorImageAtPos(): pos = " + pos);
        if (pos >= 0 && pos < mImageList.size()) {
            markAsFavor(mImageList.get(pos).getUrl());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onStart() {
        Log.v(TAG,"onStart()");
        HashMap<String,String> params = new HashMap<>();
        switch (mImageType) {
            case IMAGE_WALLPAPER:
                params.put("mIsUsed", Boolean.toString(true));
                params.put("mIsFavor", Boolean.toString(false));
                params.put("mIsWallpaper", Boolean.toString(true));
                break;
            case IMAGE_FAVOR:
                params.put("mIsUsed", Boolean.toString(true));
                params.put("mIsFavor", Boolean.toString(true));
                params.put("mIsWallpaper", Boolean.toString(false));
                break;
            case IMAGE_GALLERY:
                params.put("mIsUsed", Boolean.toString(true));
                params.put("mIsFavor", Boolean.toString(false));
                params.put("mIsWallpaper", Boolean.toString(false));
                break;

        }

        mImageList = (RealmResults<ImageRealm>) mRealmApi.queryAsync(ImageRealm.class, params);
        mImageList.addChangeListener(mOrderRealmChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG,"onDestroy()");
        //mRealmMgr.onDestroy();
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
