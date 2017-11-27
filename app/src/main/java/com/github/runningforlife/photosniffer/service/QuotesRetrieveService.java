package com.github.runningforlife.photosniffer.service;

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
import android.text.TextUtils;
import android.util.Log;

import com.github.runningforlife.photosniffer.crawler.OkHttpDownloader;
import com.github.runningforlife.photosniffer.crawler.processor.QuotePageFilter;
import com.github.runningforlife.photosniffer.crawler.processor.QuotePageProcessor;
import com.github.runningforlife.photosniffer.model.QuotePageInfo;
import com.github.runningforlife.photosniffer.model.RealmManager;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;
import us.codecraft.webmagic.Spider;

/**
 * a dedicated thread to retrieve quotes
 */

public class QuotesRetrieveService extends Service
            implements QuotePageProcessor.RetrieveCompleteCallback{
    private static final String TAG = "QuotesRetrieveService";

    public static final String EXTRA_RETRIEVE_QUOTES = "expect_quotes";
    private static final int DEFAULT_QUOTES_NUMBER = 10;
    private static final int DEFAULT_RETRIEVE_TIMEOUT = 20*1000; // 20s

    private int expect;
    private QuotePageProcessor processor;
    private Spider spider;

    private volatile H handler;
    private Looper looper;
    private ResultReceiver resultReceiver;
    private RealmResults<QuotePageInfo> quotePages;

    public QuotesRetrieveService() {
        super();
    }

    @Override
    public void onCreate(){
        super.onCreate();

        HandlerThread thread = new HandlerThread(TAG);
        thread.start();

        looper = thread.getLooper();
        handler = new H(looper);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId){
        Log.d(TAG,"onStartCommand()");
        if(intent != null){
            expect = intent.getIntExtra(EXTRA_RETRIEVE_QUOTES, DEFAULT_QUOTES_NUMBER);
            resultReceiver = intent.getParcelableExtra("receiver");

            retrieveQuotes();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        Log.v(TAG,"onDestroy()");

        processor.removeCallback(this);
    }

    private void retrieveQuotes(){
        loadQuotePages();
        if(processor == null){
            processor = new QuotePageProcessor(quotePages, expect);
            processor.addCallback(this);
        }

        spider = Spider.create(processor)
                       .setDownloader(new OkHttpDownloader());

        if(quotePages == null || quotePages.size() <= 0) {
            spider.addUrl(QuotePageFilter.QUOTE_SOURCE_0);
        }else{
            Random rnd = new Random(quotePages.size());
            int idx = rnd.nextInt(quotePages.size());
            final String url = quotePages.get(idx).getUrl();
            if(!TextUtils.isEmpty(url)) {
                spider.addUrl(url);
            }
        }
        spider.start();
        // tell client that we are starting
        handler.sendEmptyMessage(H.EVENT_RETRIEVE_START);
        // timeout message
        Message msg = handler.obtainMessage(H.EVENT_RETRIEVE_TIMEOUT);
        handler.sendMessageDelayed(msg, DEFAULT_RETRIEVE_TIMEOUT);
    }

    @Override
    public void onRetrieveComplete(int cnt) {
        Log.v(TAG,"onRetrieveComplete()");
        Message msg = handler.obtainMessage(H.EVENT_RETRIEVE_DONE, cnt);
        msg.sendToTarget();
    }

    private void loadQuotePages(){
        Realm realm = Realm.getDefaultInstance();
        quotePages = RealmManager.getAllUnvisitedQuotePages(realm);
    }

    private void handleResult(){
        sendResult();
        // stop spider
        spider.stop();
        // stop service itself
        looper.quit();
        stopSelf();
    }

    private void sendResult(){
        Log.v(TAG,"sendResult()");

        int quotes = processor.getRetrieveQuoteSize();
        if(quotes > 0){
            Bundle bundle = new Bundle();
            bundle.putInt("result", quotes);
            resultReceiver.send(ServiceStatus.SUCCESS, bundle);
        }else{
            resultReceiver.send(ServiceStatus.ERROR, null);
        }
    }

    private final class H extends Handler{

        static final int EVENT_RETRIEVE_START = 1;
        static final int EVENT_RETRIEVE_DONE = 2;
        static final int EVENT_RETRIEVE_TIMEOUT = 3;

        H(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message message){
            final int what = message.what;

            switch (what){
                case EVENT_RETRIEVE_START:
                    resultReceiver.send(ServiceStatus.RUNNING, null);
                    break;
                case EVENT_RETRIEVE_DONE:
                case EVENT_RETRIEVE_TIMEOUT:
                    handleResult();
                default:
                    break;
            }
        }

    }
}
