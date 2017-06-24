package com.github.runningforlife.photosniffer.presenter;

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

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.crawler.processor.ImageSource;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.model.RealmManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.RealmResults;
import io.realm.Sort;

import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.service.ImageRetrieveService;
import com.github.runningforlife.photosniffer.service.MyThreadFactory;
import com.github.runningforlife.photosniffer.service.ServiceStatus;
import com.github.runningforlife.photosniffer.service.SimpleResultReceiver;
import com.github.runningforlife.photosniffer.ui.GalleryView;
import com.github.runningforlife.photosniffer.utils.DisplayUtil;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

/**
 * a presenter to bridge UI and data repository
 */
public class GalleryPresenterImpl extends GalleryPresenter
        implements SimpleResultReceiver.Receiver{
    private static final String TAG = "GalleryPresenter";

    private static final int DEFAULT_RETRIEVE_TIME_OUT = 20000;
    private static final int DEFAULT_STOP_TIME_OUT = 40000;
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
    private H mMainHandler;
    private WebView mWvPage;

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
        stopRetrieveIfNeeded();

        loadPolaPageIfNeeded();

        if(mUnUsedImages == null || mUnUsedImages.size() < DEFAULT_RETRIEVED_IMAGES) {
            mIsRefreshing = true;
            Intent intent = new Intent(mContext, ImageRetrieveService.class);
            intent.putExtra("receiver", mReceiver);
            if(mUnUsedImages != null) {
                intent.putExtra(ImageRetrieveService.EXTRA_EXPECTED_IMAGES,
                        DEFAULT_RETRIEVED_IMAGES - mUnUsedImages.size());
            }else{
                intent.putExtra(ImageRetrieveService.EXTRA_EXPECTED_IMAGES, DEFAULT_RETRIEVED_IMAGES);
            }
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
            mRealmMgr.markUnusedRealm(DEFAULT_RETRIEVED_IMAGES);

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

            String imgUrl = mImageList.get(pos).getUrl();
            if(imgUrl.endsWith(ImageSource.POLA_IMAGE_END)){
                final String newUrl = imgUrl.substring(0, imgUrl.lastIndexOf("/")+1) +
                        ImageSource.POLA_FULL_IMAGE_END;
                GlideLoader.downloadOnly(mContext, newUrl, listener,
                        Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT);
            }else {
                GlideLoader.downloadOnly(mContext, imgUrl, listener,
                        Loader.DEFAULT_IMG_WIDTH, Loader.DEFAULT_IMG_HEIGHT);
            }
        }
    }

    @Override
    public void init() {
        //mRealmMgr.onStart();
        mIsRefreshing = false;
        mRealmMgr.onStart();
        //mRealmMgr.addListener(this);
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
        //releaseWakeLock();

        mRealmMgr.removeListener(this);
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

        mImageList = data;
        // unsorted: keep list descending sorted
        sort();
        mView.notifyDataChanged();

        String key = mContext.getString(R.string.pref_max_reserved_images);
        int maxReservedImage = Integer.parseInt(SharedPrefUtil.getString(key, "100"));
        if(data.size() > maxReservedImage){
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
                //releaseWakeLock();
                break;
            case ServiceStatus.SUCCESS:
                Log.v(TAG,"image retrieve success");
                if(mIsRefreshing) {
                    mView.onRefreshDone(true);
                }
                mIsRefreshing = false;
                removeMessageIfNeeded();
                //releaseWakeLock();
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

    // using webview to load pola youtu
    private void loadPolaPageIfNeeded(){
        final List<String> webSrc = SharedPrefUtil.getImageSource();
        String polaUrl = ImageSource.URL_POLA;
        if(webSrc != null && webSrc.contains(polaUrl)){
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

                    String key = mContext.getString(R.string.pref_pola_latest_collections_number);
                    int current = SharedPrefUtil.getInt(key,50);
                    if(url != null && url.endsWith("thumb.jpg")){

                        int collections = getLatestCollectionsNumber(url);
                        if(collections > current){
                            SharedPrefUtil.putInt(key, collections);
                            current = collections;
                        }

                        saveAllPolaUrls(url, current);
                    }else if(current != 50){
                        saveAllPolaUrls(ImageSource.POLA_IMAGE_START, current);
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    Log.v(TAG,"onPageFinished()");
                    view.pageDown(true);
/*                    view.loadUrl("javascript:window.HtmlViewer.retrieveHtml" +
                            "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");*/
                }
            });
            mWvPage.loadUrl(polaUrl);
        }
    }

    private int getLatestCollectionsNumber(String url){
        if(TextUtils.isEmpty(url)) return 50;

        String sub = url.substring(ImageSource.POLA_IMAGE_START.length()+1);
        String[] splits = sub.split("/");

        return Integer.parseInt(splits[0]);
    }

    private void saveAllPolaUrls(final String url, final int collections){
        if(!url.startsWith(ImageSource.POLA_IMAGE_START)) return;

        MyThreadFactory.getInstance().newThread(new Runnable() {
            @Override
            public void run() {
                List<ImageRealm> pola = new ArrayList<>();

                for(int c = 1; c < collections; ++c) {
                    for(int n = 1; n <= ImageSource.POLA_IMAGE_NUMBER_PER_COLLECTION; ++n) {
                        ImageRealm ir = new ImageRealm();
                        ir.setUrl(buildPolaImageUrl(c,n,ImageSource.POLA_IMAGE_END));
                        ir.setIsFavor(false);
                        ir.setIsWallpaper(false);
                        ir.setTimeStamp(System.currentTimeMillis());
                        ir.setUsed(false);
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
        mView.onImageSaveDone(path);
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
                    if(mIsRefreshing){
                        mView.onRefreshDone(false);
                    }
                    break;
            }
        }
    }
}
