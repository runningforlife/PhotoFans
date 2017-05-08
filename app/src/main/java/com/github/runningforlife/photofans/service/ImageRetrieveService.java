package com.github.runningforlife.photofans.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.github.runningforlife.photofans.crawler.processor.ImageRetrievePageProcessor;
import com.github.runningforlife.photofans.crawler.processor.ImageSource;
import com.github.runningforlife.photofans.realm.ImageRealm;
import com.github.runningforlife.photofans.realm.RealmHelper;

import java.util.List;

import com.github.runningforlife.photofans.crawler.OkHttpDownloader;

import us.codecraft.webmagic.Spider;

/**
 *  a service to handle image retrieve request from client
 *  client may want to give a limited number of images to retrieve at a time
 *
 *  @author JasonWang
 */

public class ImageRetrieveService extends IntentService implements
        ImageRetrievePageProcessor.RetrieveCompleteListener {

    private static final String TAG = "ImageRetrieveService";
    private static final long MAX_RETRIEVE_TIMEOUT = 30000;
    // max number of images to be retrieved a time
    public static final String EXTRA_MAX_IMAGES = "maxImages";

    private ResultReceiver mReceiver;
    private ImageRetrievePageProcessor mProcessor;
    private Spider mSpider;

    public ImageRetrieveService(){
        super("ImageRetrieveService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int max = intent.getIntExtra(EXTRA_MAX_IMAGES,0);
        mReceiver = intent.getParcelableExtra("receiver");
        if(mReceiver != null) {
            mReceiver.send(ServiceStatus.RUNNING, null);
        }

        //FIXME: cannot run this
        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // time out
                saveToRealm(mProcessor.getImageList());
                sendResult(mProcessor.getImageList().size());
            }
        }, MAX_RETRIEVE_TIMEOUT);

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

    @Override
    public void onRetrieveComplete(List<ImageRealm> data) {
        if(data == null || data.isEmpty()){
            Log.v(TAG,"onRetrieveComplete(): data is empty");
            sendResult(0);
        }else {
            Log.v(TAG,"onRetrieveComplete(): retrieved data size = " + data.size());
            saveToRealm(data);
            sendResult(data.size());
        }
    }

    private void saveToRealm(List<ImageRealm> data){
        RealmHelper.getInstance()
                .writeAsync(data);
    }

    private void sendResult(int size){
        Log.v(TAG,"sendResult(): size = " + size);
        // stop the spider
        mSpider.stop();

        Bundle bundle = new Bundle();
        bundle.putLong("result", size);
        if(size != 0) {
            mReceiver.send(ServiceStatus.SUCCESS, bundle);
        }else{
            mReceiver.send(ServiceStatus.ERROR,null);
        }
        // remove listener
        mProcessor.removeListener(this);
    }
}
