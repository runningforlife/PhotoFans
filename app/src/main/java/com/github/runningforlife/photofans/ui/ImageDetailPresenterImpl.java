package com.github.runningforlife.photofans.ui;

import android.content.Context;
import android.util.Log;

import com.github.runningforlife.photofans.realm.RealmManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.RealmResults;
import com.github.runningforlife.photofans.realm.ImageRealm;

/**
 * Created by jason on 4/6/17.
 */

public class ImageDetailPresenterImpl implements ImageDetailPresenter{
    private static final String TAG = "ImageDetailPresenter";

    private List<ImageRealm> mImgList;
    private ImageDetailView mView;
    private RealmManager mRealmMgr;

    public ImageDetailPresenterImpl(Context context, ImageDetailView view){
        mView = view;
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
    public void init() {
        mImgList = new ArrayList<>();
        mRealmMgr = RealmManager.getInstance();
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

        for(ImageRealm img : data){
            if(!mImgList.contains(img)){
                mImgList.add(img);
            }
        }

        // keep sorted
        if(mImgList.get(0).getTimeStamp() >
                mImgList.get(mImgList.size()-1).getTimeStamp()){
            Collections.sort(mImgList);
        }

        mView.onDataSetChanged();
    }
}
