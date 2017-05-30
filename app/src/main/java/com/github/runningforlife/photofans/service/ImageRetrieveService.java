package com.github.runningforlife.photofans.service;

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

import com.github.runningforlife.photofans.crawler.processor.ImageRetrievePageProcessor;
import com.github.runningforlife.photofans.crawler.processor.ImageSource;
import com.github.runningforlife.photofans.model.ImageRealm;
import com.github.runningforlife.photofans.model.RealmManager;

import java.util.List;

import com.github.runningforlife.photofans.crawler.OkHttpDownloader;

import us.codecraft.webmagic.Spider;

/**
 *  a service to handle image retrieve request from client
 *  client may want to give a limited number of images to retrieve at a time
 *
 *  @author JasonWang
 */

public class ImageRetrieveService extends Service implements
        ImageRetrievePageProcessor.RetrieveCompleteListener {

    private static final String TAG = "ImageRetrieveService";
    private volatile Looper mServiceLooper;
    private volatile H mServiceHandler;
    //private boolean mRedelivery;
    // timeout should be less than screen off timing
    private static final long MAX_RETRIEVE_TIMEOUT = 20000;
    // max number of images to be retrieved a time
    public static final String EXTRA_MAX_IMAGES = "maxImages";

    private ResultReceiver mReceiver;
    private ImageRetrievePageProcessor mProcessor;
    private Spider mSpider;

    public ImageRetrieveService(){
        super();
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"onCreate()");

        HandlerThread thread = new HandlerThread(TAG);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new H(mServiceLooper);
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
    public void onDestroy(){
        Log.v(TAG,"onDestroy()");
        mServiceLooper.quit();
        saveToRealm(mProcessor.getImageList());
        if(mSpider.getStatus() == Spider.Status.Running){
            mSpider.stop();
        }
    }

    @Override
    public void onRetrieveComplete(List<ImageRealm> data) {
        Log.v(TAG,"onRetrieveComplete()");
        // remove listener
        mProcessor.removeListener(this);
        // stop the spider
        //mSpider.close();
        mSpider.stop();
        if(!mServiceHandler.hasMessages(H.EVENT_RETRIEVE_DONE)) {
            Message msg = mServiceHandler.obtainMessage(H.EVENT_RETRIEVE_DONE);
            msg.obj = data.size();
            msg.sendToTarget();
        }

        saveToRealm(data);
    }

    private void start(Intent intent, int startId){
        Log.v(TAG,"start()");
        Message message = mServiceHandler.obtainMessage(H.EVENT_RETRIEVE_START);
        message.obj = intent;
        message.arg1 = startId;
        message.sendToTarget();

        Message timeout = mServiceHandler.obtainMessage(H.EVENT_RETRIEVE_TIMEOUT);
        timeout.arg1 = startId;
        mServiceHandler.sendMessageDelayed(timeout,MAX_RETRIEVE_TIMEOUT);
    }

    private void handleIntent(Intent intent) {
        int max = intent.getIntExtra(EXTRA_MAX_IMAGES,0);
        mReceiver = intent.getParcelableExtra("receiver");
        if(mReceiver != null) {
            mReceiver.send(ServiceStatus.RUNNING, null);
        }

        startCrawler(max);
    }

    private void startCrawler(int n){
        Log.i(TAG,"startCrawler(): max images to be retrieved = " + n);
        mProcessor = new ImageRetrievePageProcessor(n);
        mProcessor.addListener(this);
        List<String> lastUrl =  mProcessor.getStartUrl();
        if(lastUrl.size() <= 0){
            mSpider = Spider.create(mProcessor)
                    .addUrl(ImageSource.ALL_URLS)
                    .setDownloader(new OkHttpDownloader());
        }else{
            mSpider = Spider.create(mProcessor)
                    .addUrl((String[])lastUrl.toArray(new String[lastUrl.size()]))
                    .setDownloader(new OkHttpDownloader());
        }

        mSpider.run();
    }


    private void handleRetrieveComplete(int startId, int size){
        Log.v(TAG,"handleRetrieveComplete()");
        if(mServiceHandler.hasMessages(H.EVENT_RETRIEVE_TIMEOUT)){
            mServiceHandler.removeMessages(H.EVENT_RETRIEVE_TIMEOUT);
        }
        sendResult(startId,size);
    }

    private void handleTimeout(int startId){
        Log.v(TAG,"handleTimeout()");
        saveToRealm(mProcessor.getImageList());
        sendResult(startId,mProcessor.getImageList().size());
    }

    private void saveToRealm(List<ImageRealm> data){
        Log.v(TAG,"saveToRealm()");
        RealmManager.getInstance()
                .writeAsync(data);
    }

    private void sendResult(int startId, int size){
        Log.v(TAG,"sendResult(): size = " + size);
        Bundle bundle = new Bundle();
        bundle.putLong("result", size);
        if(size != 0) {
            mReceiver.send(ServiceStatus.SUCCESS, bundle);
        }else{
            mReceiver.send(ServiceStatus.ERROR,null);
        }
        // stop service
        stopSelf(startId);
    }

    private final class H extends  Handler{
        private int mStartId;

        static final int EVENT_RETRIEVE_START = 1;
        static final int EVENT_RETRIEVE_DONE = 2;
        static final int EVENT_RETRIEVE_TIMEOUT = 3;

        H(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg){
            int what = msg.what;

            switch (what){
                case EVENT_RETRIEVE_START:
                    handleIntent((Intent)msg.obj);
                    mStartId = msg.arg1;
                    break;
                case EVENT_RETRIEVE_DONE:
                    int size = (int)msg.obj;
                    handleRetrieveComplete(mStartId,size);
                    break;
                case EVENT_RETRIEVE_TIMEOUT:
                    handleTimeout(msg.arg1);
                    break;
            }
        }
    }
}
