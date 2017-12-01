package com.github.runningforlife.photosniffer.presenter;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.crawler.processor.ImageSource;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.data.local.RealmManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.service.ImageRetrieveService;
import com.github.runningforlife.photosniffer.service.MyThreadFactory;
import com.github.runningforlife.photosniffer.service.ServiceStatus;
import com.github.runningforlife.photosniffer.service.SimpleResultReceiver;
import com.github.runningforlife.photosniffer.ui.AllPictureView;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

/**
 * a presenter to bridge UI and data repository
 */
public class AllPicturesPresenterImpl implements AllPicturesPresenter,SimpleResultReceiver.Receiver{
    private static final String TAG = "AllPicturesPresenter";

    private static final int DEFAULT_RETRIEVE_TIME_OUT = 20*1000;
    private static final int DEFAULT_STOP_TIME_OUT = 40*1000;
    private static final int DEFAULT_RETRIEVED_IMAGES = 10;
    // update Pola collections every 15days
    private static final long POLA_UPDATED_DURATION = TimeUnit.DAYS.toMillis(10);
    // current latest pola
    private static final int LATEST_POLA_COUNT = 60;

    private Context mContext;
    private AllPictureView mView;
    private RealmResults<ImageRealm> mUnUsedImages;
    private RealmResults<ImageRealm> mImgList;
    //private SortedList<ImageRealm> mImgList;
    // whether user is refreshing data
    private boolean mIsRefreshing;
    // to receive result from service
    private SimpleResultReceiver mReceiver;
    private RealmManager mRealmMgr;
    private ExecutorService mExecutor;
    private H mMainHandler;
    private WebView mWvPage;
    // last removed position
    private int mLastRemovePos;
    private UpdateOp mOp;

    @SuppressWarnings("unchecked")
    public AllPicturesPresenterImpl(Context context, AllPictureView view){
        mView = view;
        mContext = context;
        mRealmMgr = RealmManager.getInstance();
        // realm only allow one transaction a time
        mExecutor = Executors.newSingleThreadExecutor();

        mMainHandler = new H(Looper.myLooper());

        //mImgList = new SortedList<ImageRealm>(ImageRealm.class, new SortedListCallback());

        mOp = UpdateOp.OP_NONE;
    }

    @Override
    public void refresh() {
        Log.v(TAG,"refresh()");
        if(!MiscUtil.isConnected(mContext)){
            mView.onNetworkDisconnect();
        }else if(MiscUtil.isMobileConnected(mContext)
                && SharedPrefUtil.isWifiOnlyDownloadMode(mContext)) {
            mView.onMobileConnected();
        }else{
            refreshAnyway();
        }
    }

    @Override
    public void refreshAnyway(){
        Log.v(TAG,"refreshAnyway()");
        // add operation
        mOp = UpdateOp.OP_ADD;
        // in case of no callback
        mRealmMgr.addUsedDataChangeListener(this);

        stopRetrieveIfNeeded();

        if(mUnUsedImages == null || mUnUsedImages.size() < DEFAULT_RETRIEVED_IMAGES) {
            startCrawlerSilent(false);
            // timeout message
            Message msg = mMainHandler.obtainMessage(H.EVENT_RETRIEVE_TIME_OUT);
            mMainHandler.sendMessageDelayed(msg, DEFAULT_RETRIEVE_TIME_OUT);
            // stop service
            Message msg1 = mMainHandler.obtainMessage(H.EVENT_STOP_SERVICE);
            mMainHandler.sendMessageDelayed(msg1, DEFAULT_STOP_TIME_OUT);
        }

        loadPolaPageIfNeeded();

        if(mUnUsedImages != null && mUnUsedImages.size() > 0) {
            // notify
            if(mUnUsedImages.size() >= DEFAULT_RETRIEVED_IMAGES){
                mIsRefreshing = false;
                mView.onRefreshDone(true);
            }
            // add unused to the list
            mRealmMgr.markUnusedRealm(DEFAULT_RETRIEVED_IMAGES);
            // silent crawling pictures
            //startCrawlerSilent(true);
        }else if(!mIsRefreshing){
            // ah, something wrong
            mView.onRefreshDone(false);
        }
    }

    @Override
    public void setWebView(WebView webView) {
        mWvPage = webView;
        if(mWvPage != null){
            WebSettings settings = mWvPage.getSettings();
            //settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setAllowContentAccess(true);
        }
    }

    @Override
    public void setWallpaperAtPos(int pos) {
        Log.v(TAG,"setWallpaperAtPos()");

        if(pos >= 0 && pos < mImgList.size()){
            setWallpaper(mImgList.get(pos).getUrl());
        }
    }

    @Override
    public void favorImageAtPos(int pos) {
        Log.v(TAG,"favorImageAtPos()");

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        ImageRealm item = mImgList.get(pos);
        item.setIsFavor(true);

        realm.commitTransaction();
    }

    @Override
    public int getItemCount() {
        if(mImgList == null) return 0;

        return mImgList.size();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        if(mImgList == null) return null;

        return mImgList.get(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        Log.v(TAG,"removeItemAtPos(): position = " + pos);
        if(mImgList == null || pos < 0) return;

        mOp = UpdateOp.OP_DELETE;
        mRealmMgr.delete(mImgList.get(pos));
    }

    @Override
    public void saveImageAtPos(final int pos) {
        Log.v(TAG,"saveImageAtPos(): pos = " + pos);
        if(pos >= 0 && pos < mImgList.size()) {
            GlideLoaderListener listener = new GlideLoaderListener(null);
            listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
                @Override
                public void onImageLoadDone(Object o) {
                    Log.d(TAG,"onImageLoadDone()");
                    if(o instanceof Bitmap) {
                        ImageSaveRunnable r = new ImageSaveRunnable(((Bitmap) o), mImgList.get(pos).getName());
                        r.addCallback(AllPicturesPresenterImpl.this);
                        mExecutor.submit(r);
                    }
                }
            });

            String imgUrl = mImgList.get(pos).getUrl();
            if(imgUrl.endsWith(ImageSource.POLA_IMAGE_END)){
                final String newUrl = imgUrl.substring(0, imgUrl.lastIndexOf("/")+1) +
                        ImageSource.POLA_FULL_IMAGE_END;
                GlideLoader.downloadOnly(mContext, newUrl, listener, Priority.HIGH,
                        Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT);
            }else {
                GlideLoader.downloadOnly(mContext, imgUrl, listener, Priority.HIGH,
                        Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT);
            }
        }
    }

    @Override
    public void init() {
        Log.v(TAG,"init()");
        mIsRefreshing = false;
        //mRealmMgr.addListener(this);
        mReceiver = new SimpleResultReceiver(new Handler(Looper.myLooper()));
        mReceiver.setReceiver(this);

        mLastRemovePos = -1;
    }

    @Override
    public void onStart() {
        Log.v(TAG,"onStart()");
        if(mImgList == null || mImgList.size() <= 0) {
            mRealmMgr.onStart();
            mRealmMgr.addUsedDataChangeListener(this);
            mRealmMgr.addUnusedDataChangeListener(this);
        }
    }

    @Override
    public void onDestroy() {
        Log.v(TAG,"onDestroy()");
        //releaseWakeLock();

        mRealmMgr.removeUsedDataChangeListener(this);
        mRealmMgr.removeUnusedDataChangeListener(this);
        //mView = null;
        mRealmMgr.onDestroy();
        // stop service
        stopRetrieveIfNeeded();
        // shut down thread pool
        mExecutor.shutdown();
        mReceiver.setReceiver(null);

        mWvPage.loadUrl(null);
        mWvPage = null;
    }

    @Override
    public void onUsedDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG,"onUsedDataChange(): data size = " + data.size());

        //sort(data);
        if(mImgList != null){
            sort(mImgList);
        }

        if(mImgList == null){
            mImgList = data;
            mView.onDataSetRangeChange(0, mImgList.size());
        }else if(mOp == UpdateOp.OP_ADD){
            mView.onDataSetRangeChange(0, DEFAULT_RETRIEVED_IMAGES);
        }else if(mOp == UpdateOp.OP_DELETE){
            mView.onDataSetRangeChange(mLastRemovePos, -1);
        }else if(mOp == UpdateOp.OP_BATCH_DELETE){
            String key = mContext.getString(R.string.pref_max_reserved_images);
            int maxReservedImage = Integer.parseInt(SharedPrefUtil.getString(key, "300"));
            mView.onDataSetRangeChange(maxReservedImage, maxReservedImage - mImgList.size());
        }

        mOp = UpdateOp.OP_NONE;

        int currentImgSize = mImgList.size();
        Log.d(TAG,"onUsedDataChange(): current size=" + currentImgSize);

        String key = mContext.getString(R.string.pref_max_reserved_images);
        int maxReservedImage = Integer.parseInt(SharedPrefUtil.getString(key, "500"));
        int diff = data.size() - maxReservedImage;
        if(diff > 0){
            mOp = UpdateOp.OP_BATCH_DELETE;
            mRealmMgr.trimData(maxReservedImage);
        }
    }

    @Override
    public void onUnusedDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG, "onUnusedDataChange(): unused url size = " + data.size());
        mUnUsedImages = data;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        switch (resultCode){
            case ServiceStatus.RUNNING:
                Log.v(TAG,"image retrieve starting");
                break;
            case ServiceStatus.ERROR:
                if(mIsRefreshing) {
                    mView.onRefreshDone(false);
                    mIsRefreshing = false;
                }
                break;
            case ServiceStatus.SUCCESS:
                Log.v(TAG,"image retrieve success");
                if(mIsRefreshing) {
                    mView.onRefreshDone(true);
                }
                mIsRefreshing = false;
                removeMessageIfNeeded();
                break;
        }
    }

    private void sort(RealmResults<ImageRealm> data){
        data.sort("mTimeStamp", Sort.DESCENDING);
    }

    private void stopRetrieveIfNeeded(){
        // try to stop service firstly
        Intent intent = new Intent(mContext,ImageRetrieveService.class);
        mContext.stopService(intent);
    }

    private void startCrawlerSilent(boolean isSilent){
        mIsRefreshing = !isSilent;
        Intent intent = new Intent(mContext, ImageRetrieveService.class);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra(ImageRetrieveService.EXTRA_EXPECTED_IMAGES, DEFAULT_RETRIEVED_IMAGES);
        mContext.startService(intent);
    }

    private void removeMessageIfNeeded(){
        mMainHandler.removeMessages(H.EVENT_RETRIEVE_TIME_OUT);
        mMainHandler.removeMessages(H.EVENT_STOP_SERVICE);
    }

    // using webview to load pola youtu
    private void loadPolaPageIfNeeded(){
        final List<String> webSrc = SharedPrefUtil.getImageSource();
        String polaUrl = ImageSource.URL_POLA;

        final String key = mContext.getString(R.string.pref_pola_latest_collections_number);
        final String lastUpdatedTime = mContext.getString(R.string.pref_pola_last_updated_time);
        final long lastTime = SharedPrefUtil.getLong(lastUpdatedTime, System.currentTimeMillis());
        long now = System.currentTimeMillis();

        String keyNewUser = mContext.getString(R.string.pref_new_user);
        boolean isFirstTimeUse = SharedPrefUtil.getBoolean(keyNewUser, true);

        if(webSrc != null && webSrc.contains(polaUrl)
                && (isFirstTimeUse || now >= lastTime + POLA_UPDATED_DURATION)){
            
            SharedPrefUtil.putBoolean(keyNewUser, false);

            String polaRetrieved = mContext.getString(R.string.pref_pola_retrieved);
            boolean isPolaRetrieved = SharedPrefUtil.getBoolean(polaRetrieved, false);
            if(!isPolaRetrieved){
                saveAllPolaUrls(polaUrl, 1, LATEST_POLA_COUNT);
                SharedPrefUtil.putBoolean(polaRetrieved, true);
                SharedPrefUtil.putLong(lastUpdatedTime, System.currentTimeMillis());
            }
            //mWvPage.addJavascriptInterface(new WebViewJsInterface(), "HtmlViewer");
            mWvPage.setWebViewClient(new WebViewClient(){

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                    webView.loadUrl(request.getUrl().toString());
                    return true;
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    Log.v(TAG,"onPageStarted()");
                    view.pageDown(true);
                }

                @Override
                public void onLoadResource(WebView view, String url) {
                    Log.v(TAG,"onLoadResource(): url = " + url);
                    view.pageDown(true);
                    int current = SharedPrefUtil.getInt(key,LATEST_POLA_COUNT);

                    if(url != null && url.endsWith("thumb.jpg")){
                        int collections = getLatestCollectionsNumber(url);
                        if(collections > current){
                            SharedPrefUtil.putInt(key, collections);
                            current = collections;
                            saveAllPolaUrls(url, current+1, collections);
                            SharedPrefUtil.putLong(lastUpdatedTime, System.currentTimeMillis());
                        }
                    }else{
                        if(System.currentTimeMillis() - lastTime >= POLA_UPDATED_DURATION){
                            SharedPrefUtil.putInt(key, current + 1);
                            // save next collections
                            saveAllPolaUrls(url, current+1, current+1);
                            SharedPrefUtil.putLong(lastUpdatedTime, System.currentTimeMillis());
                        }
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    Log.v(TAG,"onPageFinished()");
                    view.pageDown(true);
                }
            });
            mWvPage.loadUrl(polaUrl);
        }
    }

    private int getLatestCollectionsNumber(String url){
        if(TextUtils.isEmpty(url)) return LATEST_POLA_COUNT;

        String sub = url.substring(ImageSource.POLA_IMAGE_START.length()+1);
        String[] splits = sub.split("/");

        return Integer.parseInt(splits[0]);
    }

    private void saveAllPolaUrls(final String url, final int start, final int end){

        MyThreadFactory.getInstance().newThread(new Runnable() {
            @Override
            public void run() {
                List<ImageRealm> pola = new ArrayList<>();

                int count = 0;
                for(int c = start; c <= end; ++c) {
                    for(int n = 1; n <= ImageSource.POLA_IMAGE_NUMBER_PER_COLLECTION; ++n) {
                        ImageRealm ir = new ImageRealm();
                        ir.setUrl(buildPolaImageUrl(c,n,ImageSource.POLA_IMAGE_END));
                        ir.setIsFavor(false);
                        ir.setIsWallpaper(false);
                        ir.setTimeStamp(System.currentTimeMillis());
                        if(count++ < 10) {
                            ir.setUsed(true);
                        }else{
                            ir.setUsed(false);
                        }
                        ir.setName("unknown");
                        pola.add(ir);
                    }
                }

                mRealmMgr.saveImageRealmAsync(pola);
            }
        }).start();
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

    @Override
    public void onImageSaveDone(String path) {
        Log.v(TAG,"onImageSaveDone()");
        mView.onImageSaveDone(path);
    }

    private void setWallpaper(final String url){
        if(TextUtils.isEmpty(url)) return;

        GlideLoaderListener listener = new GlideLoaderListener(null);
        listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
            @Override
            public void onImageLoadDone(Object o) {
                Log.d(TAG,"onImageLoadDone()");
                if(o instanceof Bitmap) {
                    WallpaperManager wpm = WallpaperManager.getInstance(mContext);
                    try {
                        if(Build.VERSION.SDK_INT >= 24) {
                            wpm.setBitmap((Bitmap) o, null, false, WallpaperManager.FLAG_LOCK | WallpaperManager.FLAG_SYSTEM);
                        }else{
                            wpm.setBitmap((Bitmap)o);
                        }
                        mView.onWallpaperSetDone(true);
                        // mark it as wallpaper
                        mRealmMgr.setWallpaper(url);
                    } catch (IOException e) {
                        mView.onWallpaperSetDone(false);
                        e.printStackTrace();
                    }
                }else{
                    mView.onWallpaperSetDone(false);
                }
            }
        });
        GlideLoader.downloadOnly(mContext, url, listener, Priority.HIGH,
                Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT);
    }

    private final class  H extends Handler{

        static final int EVENT_RETRIEVE_TIME_OUT = 1;
        static final int EVENT_STOP_SERVICE = 2;

         H(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case EVENT_RETRIEVE_TIME_OUT:
                    mView.onRefreshDone(true);
                    mIsRefreshing = false;
                    break;
                case EVENT_STOP_SERVICE:
                    stopRetrieveIfNeeded();
                    if(mIsRefreshing){
                        mView.onRefreshDone(false);
                    }
                    break;
            }
        }
    }
}
