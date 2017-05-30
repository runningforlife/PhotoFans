package com.github.runningforlife.photofans.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.runningforlife.photofans.realm.RealmManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import com.github.runningforlife.photofans.realm.ImageRealm;
import com.github.runningforlife.photofans.service.ImageRetrieveService;
import com.github.runningforlife.photofans.service.ServiceStatus;
import com.github.runningforlife.photofans.service.SimpleResultReceiver;
import com.github.runningforlife.photofans.ui.GalleryView;
import com.github.runningforlife.photofans.utils.SharedPrefUtil;

/**
 * a presenter to bridge UI and data repository
 */
public class GalleryPresenterImpl implements GalleryPresenter,SimpleResultReceiver.Receiver{
    private static final String TAG = "GalleryPresenter";

    private static final int DEFAULT_RETRIEVED_IMAGES = 10;

    private Context mContext;
    private GalleryView mView;
    private RealmResults<ImageRealm> mUnUsedImages;
    private RealmResults<ImageRealm> mImageList;
    // whether user is refreshing data
    private boolean mIsRefreshing;
    // to receive result from service
    private SimpleResultReceiver mReceiver;
    private RealmManager mRealmMgr;
    private static int sMaxReservedImg = SharedPrefUtil.getMaxReservedImages();

    @SuppressWarnings("unchecked")
    public GalleryPresenterImpl(Context context,GalleryView view){
        mView = view;
        mContext = context;
        mRealmMgr = RealmManager.getInstance();
    }

    @Override
    public void loadAllDataAsync() {
        Log.v(TAG,"loadAllDataAsync()");
        RealmManager.getInstance().queryAllAsync();
    }

    @Override
    public void refresh() {
        Log.v(TAG,"refresh()");

        mRealmMgr.addListener(this);

        if(mUnUsedImages == null || mUnUsedImages.size() < DEFAULT_RETRIEVED_IMAGES) {
            mIsRefreshing = true;
            Intent intent = new Intent(mContext, ImageRetrieveService.class);
            intent.putExtra("receiver", mReceiver);
            intent.putExtra(ImageRetrieveService.EXTRA_MAX_IMAGES, DEFAULT_RETRIEVED_IMAGES);
            mContext.startService(intent);
        }else{
            // add to the list
            Realm realm = Realm.getDefaultInstance();

            try {
                realm.beginTransaction();
                int cn = 0;
                for (Iterator iter = mUnUsedImages.iterator();
                      iter.hasNext() && ++cn <= DEFAULT_RETRIEVED_IMAGES; ) {
                    ImageRealm item = (ImageRealm) iter.next();
                    item.setUsed(true);
                }
                realm.commitTransaction();
            }finally {
                realm.close();
            }

            mIsRefreshing = false;
        }
    }

    @Override
    public int getItemCount() {
        return mImageList.size();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        return mImageList.get(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        Log.v(TAG,"removeItemAtPos(): position = " + pos);
        mRealmMgr.delete(mImageList.get(pos));
    }

    @Override
    public void init() {
        // start earlier
        mRealmMgr.onStart();
        mIsRefreshing = false;

        mReceiver = new SimpleResultReceiver(new Handler(Looper.myLooper()));
        mReceiver.setReceiver(this);
    }

    @Override
    public void onStart() {
        mRealmMgr.addListener(this);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG,"onDestroy()");
        mRealmMgr.removeListener(this);
        //mView = null;
        mRealmMgr.onDestroy();
        mReceiver.setReceiver(null);
        // stop service
        stopRetrieveIfNeeded();
    }

    @Override
    public void onRealmDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG,"onRealmDataChange(): data size = " + data.size());
        if(data.size() > 0 && data.size() <= sMaxReservedImg) {
            if (data.get(0).getUsed()) {
                mImageList = data;
                // unsorted: keep list descending sorted
                sort();
                mView.notifyDataChanged();
            } else {
                Log.v(TAG, "onRealmDataChange(): unused url size = " + data.size());
                mUnUsedImages = data;
            }
        }else if(data.size() > sMaxReservedImg){
            mRealmMgr.trimData();
        }

        if (mIsRefreshing) {
            //mView.onRefreshDone(true);
            mIsRefreshing = false;
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        switch (resultCode){
            case ServiceStatus.RUNNING:
                Log.v(TAG,"image retrieve starting");
                break;
            case ServiceStatus.ERROR:
                mView.onRefreshDone(false);
                mIsRefreshing = false;
                break;
            case ServiceStatus.SUCCESS:
                Log.v(TAG,"image retrieve success");
                mView.onRefreshDone(true);
                mIsRefreshing = false;
                // save to realm
                break;
        }
    }

    private void sort(){
        mImageList.sort("mTimeStamp", Sort.DESCENDING);
    }

    private void stopRetrieveIfNeeded(){
        if(mIsRefreshing){
            mIsRefreshing = false;
            Intent intent = new Intent(mContext,ImageRetrieveService.class);
            mContext.stopService(intent);
        }
    }
}
