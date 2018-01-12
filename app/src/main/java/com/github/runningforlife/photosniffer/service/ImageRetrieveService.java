package com.github.runningforlife.photosniffer.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.github.runningforlife.photosniffer.crawler.processor.ImagePageFilter;
import com.github.runningforlife.photosniffer.crawler.processor.ImageRetrievePageProcessor;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;

import com.github.runningforlife.photosniffer.crawler.OkHttpDownloader;
import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.model.ImagePageInfo;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;
import com.github.runningforlife.photosniffer.utils.UrlUtil;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import us.codecraft.webmagic.Spider;

/**
 *  a service to handle image retrieveImages request from client
 *  client may want to give a limited number of images to retrieveImages at a time
 *
 *  @author JasonWang
 */

public class ImageRetrieveService extends Service {

    private static final String TAG = "ImageRetrieveService";
    private volatile Looper mServiceLooper;
    private volatile H mServiceHandler;
    private boolean mIsRetrieving;
    //private boolean mRedelivery;
    // max number of images to be retrieved a time
    public static final String EXTRA_EXPECTED_IMAGES = "maxImages";
    private static final int DEFAULT_RETRIEVE_TIME_OUT = 30*1000;
    private static final int DEFAULT_RETRIEVED_IMAGES = 10;
    private static final int MAX_SEED_URL = 5;
    private static final Random sRandom = new Random();

    private ResultReceiver mReceiver;
    private ImageRetrievePageProcessor mProcessor;
    private Spider mSpider;
    private LinkedBlockingDeque<List<String>> mRetrievedData;
    private Thread mDataSaver;
    private RealmResults<ImagePageInfo> mAllPages;
    private HashMap<String,Boolean> mPagesState;
    private ImagePageFilter mPageFilter;
    private List<String> mStartingUrl;
    private RealmApi mRealApi;


    public ImageRetrieveService(){
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate()");

        HandlerThread thread = new HandlerThread(TAG);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new H(mServiceLooper);

        mIsRetrieving = false;

        mPagesState = new HashMap<>();
        mPageFilter = new ImagePageFilter();
        mStartingUrl = new ArrayList<>();
        mRealApi = RealmApiImpl.getInstance();

        loadPages();

        mRetrievedData = new LinkedBlockingDeque<>(5);
        mDataSaver = new Thread(new Runnable() {
            @Override
            public void run() {
                RealmApi realmApi = RealmApiImpl.getInstance();
                try {
                    while (true) {
                        try {
                            List<String> data = mRetrievedData.take();
                            if (UrlUtil.isPossibleImageUrl(data.get(0))) {
                                List<ImageRealm> realmObjects = new ArrayList<>(data.size());
                                for (String url : data) {
                                    ImageRealm ir = new ImageRealm();
                                    ir.setUrl(url);
                                    realmObjects.add(ir);
                                }
                                realmApi.insertAsync(realmObjects);
                            } else {
                                List<ImagePageInfo> pages = new ArrayList<>(data.size());
                                for (String url : data) {
                                    ImagePageInfo pageInfo = new ImagePageInfo();
                                    pageInfo.setUrl(url);
                                    if (mPagesState.containsKey(url)) {
                                        pageInfo.setIsVisited(mPagesState.get(url));
                                    }
                                    pages.add(pageInfo);
                                }
                                realmApi.insertAsync(pages);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                } finally {
                    realmApi.closeRealm();
                }
            }
        },"DataSaver");

        mDataSaver.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId){
        start(intent, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG,"onDestroy()");
        if(mSpider.getStatus() == Spider.Status.Running){
            mSpider.stop();
        }
        mServiceLooper.quit();
        mDataSaver.interrupt();

        if (mAllPages.isValid()) {
            mAllPages.removeAllChangeListeners();
        }
        mRealApi.closeRealm();
    }

    private void start(Intent intent, int startId) {
        Log.v(TAG,"start()");
        if (!mIsRetrieving) {
            mIsRetrieving = false;

            Message message = mServiceHandler.obtainMessage(H.EVENT_RETRIEVE_START);
            message.obj = intent;
            message.arg1 = startId;
            message.sendToTarget();

            //timeout message
            Message msg = mServiceHandler.obtainMessage(H.EVENT_RETRIEVE_TIMEOUT);
            mServiceHandler.sendMessageDelayed(msg, DEFAULT_RETRIEVE_TIME_OUT);
        }
    }

    @SuppressLint("RestrictedApi")
    private void handleIntent(Intent intent) {
        int max = intent.getIntExtra(EXTRA_EXPECTED_IMAGES, DEFAULT_RETRIEVED_IMAGES);
        mReceiver = intent.getParcelableExtra("receiver");
        if(mReceiver != null) {
            mReceiver.send(ServiceStatus.RUNNING, null);
        }
        startCrawler(max);
    }

    private void startCrawler(int n) {
        Log.i(TAG,"startCrawler(): max images to be retrieved = " + n);
        mProcessor = new ImageRetrievePageProcessor(mRetrievedData, mStartingUrl, mPagesState, mPageFilter);
        mSpider = Spider.create(mProcessor)
                .addUrl((String[])mStartingUrl.toArray(new String[mStartingUrl.size()]))
                .setDownloader(new OkHttpDownloader());
        mSpider.run();
    }

    private void handleResult() {
        Log.v(TAG,"handleResult()");
        sendResult(mProcessor.getRetrievedImageCount());
    }

    @SuppressLint("RestrictedApi")
    private void sendResult(int size) {
        Log.v(TAG,"sendResult(): size = " + size);

        if (size != 0) {
            Bundle bundle = new Bundle();
            bundle.putLong("result", size);
            mReceiver.send(ServiceStatus.SUCCESS, bundle);
        } else {
            mReceiver.send(ServiceStatus.ERROR,null);
        }
    }

    private void loadPages() {
        mAllPages = (RealmResults<ImagePageInfo>) mRealApi.querySync(ImagePageInfo.class, null);

        for (ImagePageInfo pageInfo : mAllPages) {
            mPagesState.put(pageInfo.getUrl(), pageInfo.getIsVisited());
        }

        mAllPages.addChangeListener(new RealmChangeListener<RealmResults<ImagePageInfo>>() {
            @Override
            public void onChange(RealmResults<ImagePageInfo> element) {
                for (ImagePageInfo info : element) {
                    mPagesState.put(info.getUrl(),info.getIsVisited());
                }
            }
        });

        Log.d(TAG,"loadPages(): unvisited page size = " + mAllPages.size());
        if (mAllPages.size() > 0) {
            for (int i = 0; mStartingUrl.size() < MAX_SEED_URL && i < mAllPages.size(); ++i) {
                int idx = sRandom.nextInt(mAllPages.size());

                String url;
                try {
                    String pageUrl = mAllPages.get(idx).getUrl();
                    url = UrlUtil.getRootUrl(pageUrl);
                    if(mPageFilter.accept(url) && !mStartingUrl.contains(pageUrl)) {
                        mStartingUrl.add(pageUrl);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (mStartingUrl.size() == 0) {
            mStartingUrl.addAll(SharedPrefUtil.getImageSource());
        }

    }

    private final class H extends  Handler {
        static final int EVENT_RETRIEVE_START = 1;
        static final int EVENT_RETRIEVE_DONE = 2;
        static final int EVENT_RETRIEVE_TIMEOUT = 3;

        H (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;

            switch (what) {
                case EVENT_RETRIEVE_START:
                    handleIntent((Intent)msg.obj);
                    break;
                case EVENT_RETRIEVE_DONE:
                case EVENT_RETRIEVE_TIMEOUT:
                    handleResult();
                    break;
                default:
                    break;
            }
        }
    }
}
