package com.github.runningforlife.photosniffer.presenter;

import android.content.Context;
import android.util.Log;

import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.ui.WallpaperView;

import io.realm.RealmResults;

/**
 * wallpaper presenter to get UI data
 */

public class WallpaperPresenterImpl extends WallpaperPresenter{
    private static final String TAG = "WallpaperPresenter";
    private Context mContext;
    private WallpaperView mView;
    private RealmManager mRealm;
    private RealmResults<ImageRealm> mWallpaper;

    public WallpaperPresenterImpl(Context context, WallpaperView view){
        mContext = context;
        mView = view;
        mRealm = RealmManager.getInstance();
    }

    @Override
    public void init() {
        Log.v(TAG,"init()");
        mRealm.onStart();
    }

    @Override
    public int getItemCount() {
        checkNotNull(mWallpaper);
        return mWallpaper.size();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        checkNotNull(mWallpaper);
        return mWallpaper.get(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        checkNotNull(mWallpaper);
        mRealm.delete(mWallpaper.get(pos));
    }

    @Override
    public void saveImageAtPos(int pos) {

    }

    @Override
    public void onWallpaperDataChange(RealmResults<ImageRealm> data) {
        if(data == null){
            throw new NullPointerException("data set is null");
        }

        mWallpaper = data;

        mView.onDataSetChanged();
    }

    @Override
    public void onImageSaveDone(String path) {

    }

    @Override
    public void onStart() {
        Log.v(TAG,"onStart()");
        mRealm.addListener(this);
    }

    @Override
    public void onDestroy() {
        mRealm.onDestroy();
    }

    @Override
    public void refresh() {
        mRealm.queryAllAsync();
    }
}
