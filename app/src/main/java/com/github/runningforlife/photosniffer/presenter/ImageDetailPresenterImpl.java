package com.github.runningforlife.photosniffer.presenter;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.RequestManager;

import io.realm.RealmResults;
import io.realm.Sort;

import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.ui.ImageDetailView;

import java.util.HashMap;

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
    public void onImageTypeChange(int type) {
        Log.i(TAG,"onImageTypeChange()");
        switchToImageType(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onStart() {
        Log.v(TAG,"onStart()");
        switchToImageType(mImageType);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG,"onDestroy()");
        //mRealmMgr.onDestroy();
    }

    @Override
    protected void trimDataAsync() {
        Log.v(TAG,"trimDataAsync()");
        if (mImageList.isValid() && mImageList.size() > DEFAULT_MAX_WALLPAPERS) {
            HashMap<String, String> params = new HashMap<>();
            params.put("mIsUsed", Boolean.toString(true));
            params.put("mIsFavor", Boolean.toString(false));
            params.put("mIsWallpaper", Boolean.toString(true));
            mImageList.sort("mTimeStamp", Sort.DESCENDING);

            for (int i = DEFAULT_MAX_WALLPAPERS; i < mImageList.size(); ++i) {
                String path = mImageList.get(i).getUrl();
                mCacheMgr.remove(path);
                mRealmApi.deleteSync(mImageList.get(i));
            }
        }
    }

    @Override
    public void onImageLoadStart(int pos) {
        Log.v(TAG,"onImageLoadStart()");
        mView.onImageLoadStart(pos);
    }

    @Override
    public void onImageLoadDone(boolean isSuccess) {
        Log.v(TAG,"onImageLoadDone()");
        mView.onImageLoadDone(isSuccess);
    }

    @Override
    public void trimData() {
        Log.v(TAG,"trimData()");
        if (mImageType == IMAGE_WALLPAPER) {
            trimDataAsync();
        }
    }

    private void switchToImageType(int type) {
        if (mImageList != null) {
            mImageList.removeChangeListener(mOrderRealmChangeListener);
        }

        HashMap<String,String> params = new HashMap<>();
        switch (type) {
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

}
