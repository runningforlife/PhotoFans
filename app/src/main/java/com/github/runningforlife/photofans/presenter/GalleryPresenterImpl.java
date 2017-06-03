package com.github.runningforlife.photofans.presenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.runningforlife.photofans.loader.GlideLoader;
import com.github.runningforlife.photofans.loader.GlideLoaderListener;
import com.github.runningforlife.photofans.model.RealmManager;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import com.github.runningforlife.photofans.model.ImageRealm;
import com.github.runningforlife.photofans.service.ImageRetrieveService;
import com.github.runningforlife.photofans.service.ServiceStatus;
import com.github.runningforlife.photofans.service.SimpleResultReceiver;
import com.github.runningforlife.photofans.ui.GalleryView;
import com.github.runningforlife.photofans.utils.DisplayUtil;
import com.github.runningforlife.photofans.utils.MediaStoreUtil;
import com.github.runningforlife.photofans.utils.SharedPrefUtil;
import com.github.runningforlife.photofans.utils.ThreadUtil;

/**
 * a presenter to bridge UI and data repository
 */
public class GalleryPresenterImpl implements GalleryPresenter,SimpleResultReceiver.Receiver{
    private static final String TAG = "GalleryPresenter";

    private static final int DEFAULT_RETRIEVED_IMAGES = 10;
    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = (int)(DEFAULT_WIDTH*DisplayUtil.getScreenRatio());

    private Context mContext;
    private GalleryView mView;
    private RealmResults<ImageRealm> mUnUsedImages;
    private RealmResults<ImageRealm> mImageList;
    // whether user is refreshing data
    private boolean mIsRefreshing;
    // to receive result from service
    private SimpleResultReceiver mReceiver;
    private RealmManager mRealmMgr;
    private ExecutorService mExecutor;
    private static int sMaxReservedImg = SharedPrefUtil.getMaxReservedImages();
    private int mPrevImgCount;
    @SuppressWarnings("unchecked")
    public GalleryPresenterImpl(Context context,GalleryView view){
        mView = view;
        mContext = context;
        mRealmMgr = RealmManager.getInstance();
        // realm only allow one transaction a time
        mExecutor = Executors.newSingleThreadExecutor();
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
        Log.v(TAG,"removeItemAtPos(): position = " + pos);
        if(mImageList == null) return;

        mRealmMgr.delete(mImageList.get(pos));
    }

    @Override
    public void saveImageAtPos(final int pos) {
        Log.v(TAG,"saveImageAtPos(): pos = " + pos);
        if(pos >= 0 && pos < mImageList.size()) {
            GlideLoaderListener listener = new GlideLoaderListener(null);
            listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
                @Override
                public void onImageLoadDone(Object o) {
                    Log.d(TAG,"onImageLoadDone()");
                    ImageSaveRunnable r = new ImageSaveRunnable(((Bitmap)o), mImageList.get(pos).getName());
                    r.addCallback(GalleryPresenterImpl.this);
                    mExecutor.submit(r);
                }
            });
            GlideLoader.downloadOnly(mContext, mImageList.get(pos).getUrl(), listener,
                    DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }
    }

    @Override
    public void init() {
        // start earlier
        mRealmMgr.addListener(this);
        mRealmMgr.onStart();
        mIsRefreshing = false;

        mReceiver = new SimpleResultReceiver(new Handler(Looper.myLooper()));
        mReceiver.setReceiver(this);
    }

    @Override
    public void onStart() {
        Log.v(TAG,"onStart()");
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
        // shut down thread pool
        mExecutor.shutdown();
    }

    @Override
    public void onRealmDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG,"onRealmDataChange(): data size = " + data.size());
        // data size is not changed, just return
        if(mPrevImgCount == data.size()) {
            mView.notifyDataChanged();
            return;
        }

        mPrevImgCount = data.size();
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

    @Override
    public void onImageSaveDone(String path) {
        mView.onImageSaveDone(path);
    }
}
