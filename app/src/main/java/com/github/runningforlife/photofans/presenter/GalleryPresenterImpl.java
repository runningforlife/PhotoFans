package com.github.runningforlife.photofans.presenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.runningforlife.photofans.model.RealmManager;

import java.io.IOException;
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
import com.github.runningforlife.photofans.utils.BitmapUtil;
import com.github.runningforlife.photofans.utils.DisplayUtil;
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
    private ExecutorService mExecutor;
    private static int sMaxReservedImg = SharedPrefUtil.getMaxReservedImages();
    private int mPrevImgCount;
    @SuppressWarnings("unchecked")
    public GalleryPresenterImpl(Context context,GalleryView view){
        mView = view;
        mContext = context;
        mRealmMgr = RealmManager.getInstance();
        mExecutor = Executors.newFixedThreadPool(5);
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
    public void saveImageAtPos(int pos, Bitmap bitmap) {
        Log.v(TAG,"saveImageAtPos(): pos = " + pos);
        mExecutor.submit(new BitmapSaveRunnable(mImageList.get(pos),bitmap));
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

        if(data.size() > 0 && data.size() <= sMaxReservedImg) {
            // save the current image count
            mPrevImgCount = data.size();
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

    private class BitmapSaveRunnable implements Runnable{
        private ImageRealm cache;
        private Bitmap bitmap;

        public BitmapSaveRunnable(ImageRealm cache, Bitmap bitmap){
            this.cache = cache;
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            Bitmap bm = Bitmap.createScaledBitmap(bitmap,256,(int)(256*DisplayUtil.getScreenRatio()),false);

            byte[] bytes = new byte[256*256*2];
            try {
                BitmapUtil.getBytes(bytes,bm,100);
                Realm r = Realm.getDefaultInstance();
                r.beginTransaction();
                cache.setData(bytes);
                r.commitTransaction();
            } catch (IOException e) {
                bytes = null;
                e.printStackTrace();
            }
        }
    }
}
