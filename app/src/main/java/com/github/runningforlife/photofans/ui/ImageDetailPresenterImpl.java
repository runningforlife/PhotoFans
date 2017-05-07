package com.github.runningforlife.photofans.ui;

import android.content.Context;
import android.util.Log;

import com.github.runningforlife.photofans.model.RealmHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.RealmResults;
import com.github.runningforlife.photofans.model.ImageRealm;

/**
 * Created by jason on 4/6/17.
 */

public class ImageDetailPresenterImpl implements ImageDetailPresenter{
    private static final String TAG = "ImagePreviewPresenter";

    private List<ImageRealm> mImgList;
    private ImageDetailView mView;
    private RealmHelper mHelper;

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
        mHelper = RealmHelper.getInstance();
        // start earlier
        mHelper.onStart();
    }

    @Override
    public void onResume() {
        mHelper.addListener(this);
    }

    @Override
    public void onDestroy() {
        mHelper.removeListener(this);
        mHelper.onDestroy();
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
