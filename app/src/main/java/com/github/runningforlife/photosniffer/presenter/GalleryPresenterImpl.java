package com.github.runningforlife.photosniffer.presenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.model.RealmManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.service.ImageRetrieveService;
import com.github.runningforlife.photosniffer.service.ServiceStatus;
import com.github.runningforlife.photosniffer.service.SimpleResultReceiver;
import com.github.runningforlife.photosniffer.ui.GalleryView;
import com.github.runningforlife.photosniffer.utils.DisplayUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

/**
 * a presenter to bridge UI and data repository
 */
public class GalleryPresenterImpl extends GalleryPresenter
        implements SimpleResultReceiver.Receiver{
    private static final String TAG = "GalleryPresenter";

    private static final int DEFAULT_RETRIEVE_TIME_OUT = 20000;
    private static final int DEFAULT_STOP_TIME_OUT = 30000;
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
    private H mMainHandler;

    @SuppressWarnings("unchecked")
    public GalleryPresenterImpl(Context context,GalleryView view){
        mView = view;
        mContext = context;
        mRealmMgr = RealmManager.getInstance();
        // realm only allow one transaction a time
        mExecutor = Executors.newSingleThreadExecutor();

        mMainHandler = new H(Looper.myLooper());
    }

    @Override
    public void refresh() {
        Log.v(TAG,"refresh()");

        stopRetrieveIfNeeded();

        if(mUnUsedImages == null || mUnUsedImages.size() < DEFAULT_RETRIEVED_IMAGES) {
            mIsRefreshing = true;
            Intent intent = new Intent(mContext, ImageRetrieveService.class);
            intent.putExtra("receiver", mReceiver);
            intent.putExtra(ImageRetrieveService.EXTRA_EXPECTED_IMAGES, DEFAULT_RETRIEVED_IMAGES - mUnUsedImages.size());
            mContext.startService(intent);

            // timeout message
            Message msg = mMainHandler.obtainMessage(H.EVENT_RETRIEVE_TIME_OUT);
            mMainHandler.sendMessageDelayed(msg, DEFAULT_RETRIEVE_TIME_OUT);
            // stop service
            Message msg1 = mMainHandler.obtainMessage(H.EVENT_STOP_SERVICE);
            mMainHandler.sendMessageDelayed(msg1, DEFAULT_STOP_TIME_OUT);
        }

        if(mUnUsedImages != null && mUnUsedImages.size() > 0) {
            // notify
            if(mUnUsedImages.size() >= DEFAULT_RETRIEVED_IMAGES){
                mIsRefreshing = false;
                mView.onRefreshDone(true);
            }
            // add unused to the list
            Realm realm = Realm.getDefaultInstance();
            try {
                int cn = 0;
                realm.beginTransaction();
                for (Iterator iter = mUnUsedImages.iterator();
                     iter.hasNext() && ++cn <= DEFAULT_RETRIEVED_IMAGES; ) {
                    ImageRealm item = (ImageRealm) iter.next();
                    item.setUsed(true);
                    // update time stamp
                    item.setTimeStamp(System.currentTimeMillis());
                }
                realm.commitTransaction();
            } finally {
                realm.close();
            }

        }else if(!mIsRefreshing){
            // ah, something wrong
            mView.onRefreshDone(false);
        }
        // when refresh, currently keep screen on
        wakeup(DEFAULT_RETRIEVE_TIME_OUT);
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
        //mRealmMgr.onStart();
        mIsRefreshing = false;
        mRealmMgr.onStart();
        mRealmMgr.addListener(this);
        mReceiver = new SimpleResultReceiver(new Handler(Looper.myLooper()));
        mReceiver.setReceiver(this);
    }

    @Override
    public void onStart() {
        Log.v(TAG,"onStart()");
        // start earlier
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
    public void onUsedRealmDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG,"onUsedRealmDataChange(): data size = " + data.size());
        // data size is not changed, just return
/*        if(mPrevImgCount == data.size()) {
            mView.notifyDataChanged();
            return;
        }*/

        mPrevImgCount = data.size();
        if(data.size() > 0 && data.size() <= sMaxReservedImg) {
            mImageList = data;
            // unsorted: keep list descending sorted
            sort();
            mView.notifyDataChanged();
        }else if(data.size() > sMaxReservedImg){
            mRealmMgr.trimData();
        }
    }

    @Override
    public void onUnusedRealmDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG, "onUnusedRealmDataChange(): unused url size = " + data.size());
        mUnUsedImages = data;
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
                if(mIsRefreshing) {
                    mView.onRefreshDone(true);
                }
                mIsRefreshing = false;
                removeMessageIfNeeded();
                // save to realm
                break;
        }
    }

    private void sort(){
        mImageList.sort("mTimeStamp", Sort.DESCENDING);
    }

    private void stopRetrieveIfNeeded(){
        // try to stop service firstly
        Intent intent = new Intent(mContext,ImageRetrieveService.class);
        mContext.stopService(intent);
    }

    private void removeMessageIfNeeded(){
        mMainHandler.removeMessages(H.EVENT_RETRIEVE_TIME_OUT);
        mMainHandler.removeMessages(H.EVENT_STOP_SERVICE);
    }

    @Override
    public void onImageSaveDone(String path) {
        mView.onImageSaveDone(path);
    }

    private void wakeup(long time){
        String pmName = "android.os.PowerManager";
        String wakeup = "wakeUp";
        try {
            Class cl = Class.forName(pmName);
            Method m = cl.getMethod(wakeup,Long.class);
            m.invoke(cl.newInstance(), time);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private final class  H extends Handler{

        static final int EVENT_RETRIEVE_TIME_OUT = 1;
        static final int EVENT_STOP_SERVICE = 2;

        public H(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg){

            int w = msg.what;

            switch (w){
                case EVENT_RETRIEVE_TIME_OUT:
                    mView.onRefreshDone(true);
                    mIsRefreshing = false;
                    break;
                case EVENT_STOP_SERVICE:
                    stopRetrieveIfNeeded();
                    break;
            }
        }
    }
}
