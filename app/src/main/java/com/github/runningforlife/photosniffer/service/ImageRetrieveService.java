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

import com.github.runningforlife.photosniffer.crawler.processor.ImageRetrievePageProcessor;

import java.util.List;

import com.github.runningforlife.photosniffer.crawler.OkHttpDownloader;
import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import us.codecraft.webmagic.Spider;

/**
 *  a service to handle image retrieveImages request from client
 *  client may want to give a limited number of images to retrieveImages at a time
 *
 *  @author JasonWang
 */

public class ImageRetrieveService extends Service implements
        ImageRetrievePageProcessor.RetrieveCompleteListener {

    private static final String TAG = "ImageRetrieveService";
    private volatile Looper mServiceLooper;
    private volatile H mServiceHandler;
    private boolean mIsRetrieving;
    //private boolean mRedelivery;
    // max number of images to be retrieved a time
    public static final String EXTRA_EXPECTED_IMAGES = "maxImages";
    private static final int DEFAULT_RETRIEVE_TIME_OUT = 30*1000;

    private ResultReceiver mReceiver;
    private ImageRetrievePageProcessor mProcessor;
    private Spider mSpider;
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

        //mRealApi = RealmApiImpl.getInstance();
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

        mProcessor.stopGracefully();

        sendResult(mProcessor.getRetrievedImageCount());

        mProcessor.removeListener(this);
        if(mSpider.getStatus() == Spider.Status.Running){
            mSpider.stop();
        }

        mServiceLooper.quit();

        //mRealApi.closeRealm();
    }

    @Override
    public void onExpectedComplete(int size) {
        Log.v(TAG,"onExpectedComplete()");
        if(!mServiceHandler.hasMessages(H.EVENT_RETRIEVE_DONE)) {
            Message msg = mServiceHandler.obtainMessage(H.EVENT_RETRIEVE_DONE);
            msg.obj = size;
            msg.sendToTarget();
        }
    }

    @Override
    public void onRetrieveComplete(int size) {
        Log.v(TAG,"onRetrieveComplete()");
        // ok, got what we want, clear up
        clearUp();
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
        int max = intent.getIntExtra(EXTRA_EXPECTED_IMAGES, 10);
        mReceiver = intent.getParcelableExtra("receiver");
        if(mReceiver != null) {
            mReceiver.send(ServiceStatus.RUNNING, null);
        }

        startCrawler(max);
    }

    private void startCrawler(int n) {
        Log.i(TAG,"startCrawler(): max images to be retrieved = " + n);
        mProcessor = new ImageRetrievePageProcessor(n);
        mProcessor.addListener(this);
        List<String> lastUrl =  mProcessor.getStartUrl();
        if (lastUrl.size() <= 0) {
            List<String> defList = SharedPrefUtil.getImageSource();
            String[] defSource = defList.toArray(new String[defList.size()]);
            mSpider = Spider.create(mProcessor)
                    .addUrl(defSource)
                    .setDownloader(new OkHttpDownloader());
        } else {
            mSpider = Spider.create(mProcessor)
                    .addUrl((String[])lastUrl.toArray(new String[lastUrl.size()]))
                    .setDownloader(new OkHttpDownloader());
        }

        mSpider.run();
    }

    private void handleResult() {
        Log.v(TAG,"handleTimeout()");
        sendResult(mProcessor.getRetrievedImageCount());
    }

    private void clearUp() {
        mProcessor.removeListener(this);
        if(mSpider.getStatus() == Spider.Status.Running){
            mSpider.stop();
        }
        // stop service
        stopSelf();
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
