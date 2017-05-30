package com.github.runningforlife.photofans.presenter;

import android.content.Context;
import android.util.Log;

import com.github.runningforlife.photofans.model.RealmManager;

import io.realm.RealmResults;
import io.realm.Sort;

import com.github.runningforlife.photofans.model.ImageRealm;
import com.github.runningforlife.photofans.ui.ImageDetailView;

/**
 * Created by jason on 4/6/17.
 */

public class ImageDetailPresenterImpl implements ImageDetailPresenter {
    private static final String TAG = "ImageDetailPresenter";

    private RealmResults<ImageRealm> mImgList;
    private ImageDetailView mView;
    private RealmManager mRealmMgr;

    public ImageDetailPresenterImpl(Context context, ImageDetailView view){
        mView = view;
        mRealmMgr = RealmManager.getInstance();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        return mImgList.get(pos);
    }

    @Override
    public int getItemCount() {
        return mImgList.size();
    }

    @Override
    public void removeItemAtPos(int pos) {
        Log.d(TAG,"removeItemAtPos()");
        mRealmMgr.delete(mImgList.get(pos));
    }

    @Override
    public void init() {
        //mImgList = new ArrayList<>();
        mRealmMgr.onStart();
    }

    @Override
    public void onStart() {
        Log.v(TAG,"onStart()");
        mRealmMgr.addListener(this);
    }

    @Override
    public void onDestroy() {
        mRealmMgr.removeListener(this);
        mRealmMgr.onDestroy();
    }

    @Override
    public void onRealmDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG,"onRealmDataChange(): data size = " + data.size());
        mImgList = data;
        // keep sorted
        sort();
        mView.onDataSetChanged();
    }

    private void sort(){
        mImgList.sort("mTimeStamp", Sort.DESCENDING);
    }
}
