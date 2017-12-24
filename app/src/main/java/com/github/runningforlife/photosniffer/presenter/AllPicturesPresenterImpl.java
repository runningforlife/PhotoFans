package com.github.runningforlife.photosniffer.presenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.crawler.processor.ImageSource;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.service.ImageRetrieveService;
import com.github.runningforlife.photosniffer.service.ServiceStatus;
import com.github.runningforlife.photosniffer.service.SimpleResultReceiver;
import com.github.runningforlife.photosniffer.ui.AllPictureView;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;
import com.github.runningforlife.photosniffer.utils.WallpaperUtils;

/**
 * a presenter to bridge UI and data repository
 */
public class AllPicturesPresenterImpl extends PresenterBase
        implements AllPicturesPresenter,SimpleResultReceiver.Receiver{
    private static final String TAG = "AllPicturesPresenter";

    private static final int DEFAULT_RETRIEVE_TIME_OUT = 20*1000;
    private static final int DEFAULT_STOP_TIME_OUT = 40*1000;
    private static final int DEFAULT_RETRIEVED_IMAGES = 10;
    // updateAsync Pola collections every 7days
    private static final long POLA_UPDATED_DURATION = TimeUnit.DAYS.toMillis(8);
    // current latest pola
    private static final int LATEST_POLA_COUNT = 73;

    private RealmResults<ImageRealm> mUnUsedImages;
    // whether user is refreshing data
    private boolean mIsRefreshing;
    // to receive result from service
    private SimpleResultReceiver mReceiver;
    private ExecutorService mExecutor;
    private H mMainHandler;
    // last removed position

    @SuppressWarnings("unchecked")
    public AllPicturesPresenterImpl(Context context, AllPictureView view) {
        super(context, view);
        // realm only allow one transaction a time
        mExecutor = Executors.newSingleThreadExecutor();

        mMainHandler = new H(Looper.myLooper());

        String maxImages = SharedPrefUtil.getString(context.getString(R.string.pref_max_reserved_images), "1000");
        int maxIntLen = Integer.toString(Integer.MAX_VALUE).length();
        if (maxImages.length() < maxIntLen) {
            try {
                mMaxImagesAllowed = Integer.parseInt(maxImages);
            } catch (Exception e) {
                // number exception, just ingore
            }
        }
    }

    @Override
    public void refresh() {
        Log.v(TAG,"refresh()");
        if (!MiscUtil.isConnected(mContext)) {
            ((AllPictureView)mView).onNetworkDisconnect();
        } else if (MiscUtil.isMobileConnected(mContext) && SharedPrefUtil.isWifiOnlyDownloadMode(mContext)) {
            ((AllPictureView)mView).onMobileConnected();
        } else {
            refreshAnyway();
        }
    }

    @Override
    public void refreshAnyway() {
        Log.v(TAG,"refreshAnyway()");
        // add operation
        loadPolaPageIfNeeded();

        if(mUnUsedImages == null || mUnUsedImages.size() < 3*DEFAULT_RETRIEVED_IMAGES) {
            startCrawlerSilent(false);
            // timeout message
            Message msg = mMainHandler.obtainMessage(H.EVENT_RETRIEVE_TIME_OUT);
            mMainHandler.sendMessageDelayed(msg, DEFAULT_RETRIEVE_TIME_OUT);
            // stop service
            Message msg1 = mMainHandler.obtainMessage(H.EVENT_STOP_SERVICE);
            mMainHandler.sendMessageDelayed(msg1, DEFAULT_STOP_TIME_OUT);
        }

        if (mUnUsedImages != null && mUnUsedImages.size() > 0) {
            if(mUnUsedImages.size() >= DEFAULT_RETRIEVED_IMAGES) {
                mIsRefreshing = false;
                ((AllPictureView)mView).onRefreshDone(true);
            }
            markUnUsedRealm(DEFAULT_RETRIEVED_IMAGES);
        } else if (!mIsRefreshing) {
            // ah, something wrong
            ((AllPictureView)mView).onRefreshDone(false);
        }

        // start wallpaper cache service
        Message message = mMainHandler.obtainMessage(H.EVENT_START_WALLPAPER_CACHE);
        mMainHandler.sendMessageDelayed(message, 500);
    }

    @Override
    public void favorImageAtPos(int pos) {
        Log.v(TAG,"favorImageAtPos()");
        markAsFavor(mImageList.get(pos).getUrl());
    }

    @Override
    public void onStart() {
        Log.v(TAG,"onStart()");
        HashMap<String,String> params = new HashMap<>();
        params.put("mIsUsed", Boolean.toString(true));
        params.put("mIsFavor", Boolean.toString(false));
        params.put("mIsWallpaper", Boolean.toString(false));
        mImageList = (RealmResults<ImageRealm>) mRealmApi.queryAsync(ImageRealm.class, params);
        mImageList.addChangeListener(mOrderRealmChangeListener);

        HashMap<String, String> unUsedParams = new HashMap<>();
        unUsedParams.put("mIsUsed", Boolean.toString(false));
        mUnUsedImages = (RealmResults<ImageRealm>) mRealmApi.queryAsync(ImageRealm.class, unUsedParams);
        mUnUsedImages.addChangeListener(new RealmChangeListener<RealmResults<ImageRealm>>() {
            @Override
            public void onChange(RealmResults<ImageRealm> element) {
                Log.v(TAG,"onChange():size=" + element.size());
            }
        });

        mIsRefreshing = false;
        //mRealmMgr.addListener(this);
        mReceiver = new SimpleResultReceiver(new Handler(Looper.myLooper()));
        mReceiver.setReceiver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG,"onDestroy()");
        if (mUnUsedImages.isValid()) {
            mUnUsedImages.removeAllChangeListeners();
        }
        // stop service
        stopRetrieveIfNeeded();
        // shut down thread pool
        if (!mExecutor.isShutdown()) {
            mExecutor.shutdown();
        }
        mReceiver.setReceiver(null);
    }


    @Override
    public void onImageSaveDone(String path) {
        Log.v(TAG,"onImageSaveDone()");
        mView.onImageSaveDone(path);
    }


    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        switch (resultCode) {
            case ServiceStatus.RUNNING:
                Log.v(TAG,"image retrieveImages starting");
                break;
            case ServiceStatus.ERROR:
                if (mIsRefreshing) {
                    ((AllPictureView)mView).onRefreshDone(false);
                    mIsRefreshing = false;
                }
                break;
            case ServiceStatus.SUCCESS:
                Log.v(TAG,"image retrieveImages success");
                if (mIsRefreshing) {
                    ((AllPictureView)mView).onRefreshDone(true);
                }
                mIsRefreshing = false;
                removeMessageIfNeeded();
                break;
        }
    }

    private void markUnUsedRealm(int num) {
        Log.v(TAG,"markUnUsedRealm()");
        mRealmApi.markUnusedRealm(num);
    }

    private void stopRetrieveIfNeeded() {
        // try to stop service firstly
        Intent intent = new Intent(mContext,ImageRetrieveService.class);
        mContext.stopService(intent);
    }

    private void startCrawlerSilent(boolean isSilent) {
        mIsRefreshing = !isSilent;
        Intent intent = new Intent(mContext, ImageRetrieveService.class);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra(ImageRetrieveService.EXTRA_EXPECTED_IMAGES, DEFAULT_RETRIEVED_IMAGES);
        mContext.startService(intent);
    }

    private void removeMessageIfNeeded() {
        mMainHandler.removeMessages(H.EVENT_RETRIEVE_TIME_OUT);
        mMainHandler.removeMessages(H.EVENT_STOP_SERVICE);
    }

    // using webview to load pola youtu
    private void loadPolaPageIfNeeded() {
        final String lastUpdatedTime = mContext.getString(R.string.pref_pola_last_updated_time);
        final long lastTime = SharedPrefUtil.getLong(lastUpdatedTime, System.currentTimeMillis());
        long now = System.currentTimeMillis();

        String keyNewUser = mContext.getString(R.string.pref_new_user);
        boolean isFirstTimeUse = SharedPrefUtil.getBoolean(keyNewUser, true);

        if ((isFirstTimeUse || now >= lastTime + POLA_UPDATED_DURATION)) {
            
            SharedPrefUtil.putBoolean(keyNewUser, false);

            String polaRetrieved = mContext.getString(R.string.pref_pola_retrieved);
            boolean isPolaRetrieved = SharedPrefUtil.getBoolean(polaRetrieved, false);
            if (!isPolaRetrieved) {
                saveAllPolaUrls(1, LATEST_POLA_COUNT);
                SharedPrefUtil.putBoolean(polaRetrieved, true);
                SharedPrefUtil.putLong(lastUpdatedTime, System.currentTimeMillis());
            }
        }
    }

    private void saveAllPolaUrls(final int start, final int end){
        List<ImageRealm> pola = new ArrayList<>();
        List<String> builtInWallpapers = SharedPrefUtil.getArrayList(mContext.getString(R.string.build_in_wallpaper_list));
        int count = 0;
        for(int c = start; c <= end; ++c) {
            for(int n = 1; n <= ImageSource.POLA_IMAGE_NUMBER_PER_COLLECTION; ++n) {
                ImageRealm ir = new ImageRealm();
                String url = buildPolaImageUrl(c,n,ImageSource.POLA_IMAGE_END);
                if (!builtInWallpapers.contains(url)) {
                    ir.setUrl(url);
                    ir.setIsFavor(false);
                    ir.setIsWallpaper(false);
                    ir.setIsCached(false);
                    ir.setTimeStamp(System.currentTimeMillis());
                    if (count++ < 10) {
                        ir.setUsed(true);
                    } else {
                        ir.setUsed(false);
                    }
                    ir.setName("unknown");
                    pola.add(ir);
                }
            }
        }

        mRealmApi.insertAsync(pola);
    }

    private String buildPolaImageUrl(int collection, int number, String end){
        StringBuilder builder = new StringBuilder(ImageSource.POLA_IMAGE_START);
        builder.append("/")
                .append(collection)
                .append("/")
                .append(number)
                .append("/")
                .append(end);

        return builder.toString();
    }


    private void startUpdateWallpaperCache() {
        // firstly, we try to fill wallpaper cache, and then
        // start a scheduled job to update cache
        //WallpaperUtils.startWallpaperCacheUpdaterService(mContext);
        if (Build.VERSION.SDK_INT >= 21) {
            WallpaperUtils.startWallpaperUpdaterJob(mContext, MiscUtil.getJobId(MiscUtil.JOB_WALLPAPER_CACHE));
        } else {
            WallpaperUtils.startWallpaperCacheUpdaterAlarm(mContext);
        }
    }

    private final class  H extends Handler {

        static final int EVENT_RETRIEVE_TIME_OUT = 1;
        static final int EVENT_STOP_SERVICE = 2;
        static final int EVENT_START_WALLPAPER_CACHE = 3;

         H(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case EVENT_RETRIEVE_TIME_OUT:
                    ((AllPictureView)mView).onRefreshDone(true);
                    mIsRefreshing = false;
                    break;
                case EVENT_STOP_SERVICE:
                    stopRetrieveIfNeeded();
                    if(mIsRefreshing){
                        ((AllPictureView)mView).onRefreshDone(false);
                    }
                    break;
                case EVENT_START_WALLPAPER_CACHE:
                    startUpdateWallpaperCache();
                    break;
            }
        }
    }
}
