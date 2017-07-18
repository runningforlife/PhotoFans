package com.github.runningforlife.photosniffer.service;

import android.os.HandlerThread;
import android.util.Log;

import com.github.runningforlife.photosniffer.crawler.OkHttpDownloader;
import com.github.runningforlife.photosniffer.crawler.processor.QuotePageFilter;
import com.github.runningforlife.photosniffer.crawler.processor.QuotePageProcessor;
import com.github.runningforlife.photosniffer.crawler.processor.QuotesRetriever;

import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * a dedicated thread to retrieve quotes
 */

public class QuotesRetrieveThread extends Thread
            implements QuotePageProcessor.RetrieveCompleteCallback{
    private static final String TAG = "QuotesRetrieveThread";

    private int expect;
    private QuoteRetrieveCallback callback;
    private PageProcessor processor;

    public interface QuoteRetrieveCallback{
        void onRetrieveComplete(boolean success);
    }

    public void setCallback(QuoteRetrieveCallback callback){
        this.callback = callback;
    }

    public QuotesRetrieveThread(int expect){
        this(TAG);

        this.expect = expect;
    }

    private QuotesRetrieveThread(String name) {
        super(name);
    }

    @Override
    public void run(){
        super.run();

        retrieveQuotes();
    }

    public void setProcessor(PageProcessor processor){
        this.processor = processor;
    }

    private void retrieveQuotes(){
        if(processor == null){
            processor = new QuotePageProcessor(expect);
        }

        Spider.create(processor)
              .setDownloader(new OkHttpDownloader())
              .addUrl(QuotePageFilter.QUOTE_SOURCE_0)
              .start();
    }

    @Override
    public void onRetrieveComplete(boolean success) {
        Log.v(TAG,"onRetrieveComplete()");
        callback.onRetrieveComplete(true);
    }
}
