package com.github.runningforlife.photosniffer.crawler.processor;

import android.util.Log;

import com.github.runningforlife.photosniffer.model.QuotePageInfo;
import com.github.runningforlife.photosniffer.model.QuoteRealm;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * an page processor to retrieve quotes
 */

public class QuotePageProcessor implements PageProcessor, QuotesRetriever.RetrieveCompleteListener{
    private static final String TAG = "QuotePageProcessor";

    private static final int DEFAULT_RETRIEVED_QUOTES = 10;

    private Site site = Site.me().setRetryTimes(3)
            .setSleepTime(1000).setTimeOut(3000);

    private QuotePageFilter filter;
    private QuotesRetriever retriever;
    private List<RetrieveCompleteCallback> callback;

    public interface RetrieveCompleteCallback{
        void onRetrieveComplete(int cnt);
    }

    public void addCallback(RetrieveCompleteCallback cb){
        if(!callback.contains(cb)) {
            callback.add(cb);
        }
    }

    public void removeCallback(RetrieveCompleteCallback cb){
        if(callback != null){
            callback.remove(cb);
        }
    }

    public QuotePageProcessor(RealmResults<QuotePageInfo> pages, int expected){
        int expect = expected <= 0 ? DEFAULT_RETRIEVED_QUOTES : expected;
        filter = new QuotePageFilter();
        retriever = new QuotesRetriever(pages, expect);
        retriever.setCompleteListener(this);

        callback = new ArrayList<>();
    }

    @Override
    public void process(Page page) {
        Log.v(TAG,"process()");
        int statusCode = page.getStatusCode();
        if(statusCode == 200){
            retrieveQuotes(page);
        }if(statusCode >= 400 && statusCode <= 511){
            Log.e(TAG,"process(): fail to download pages");
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    private void retrieveQuotes(Page page) {
        Log.v(TAG, "retrieveQuotes()");
        if (filter.accept(page.getUrl().get())) {
            retriever.retrieve(page);
        }
    }

    @Override
    public void onRetrieveComplete(int cnt) {
        Log.v(TAG,"onRetrieveComplete()");
        if(cnt > 0){
            for(RetrieveCompleteCallback cb : callback){
                cb.onRetrieveComplete(cnt);
            }
        }else{
            for(RetrieveCompleteCallback cb : callback){
                cb.onRetrieveComplete(-1);
            }
        }
    }

    public int getRetrieveQuoteSize(){
        return  retriever.getQuoteList().size();
    }

    // for test
    public List<QuotePageInfo> getPageList(){
        return retriever.getPageList();
    }

    // for test
    public List<QuoteRealm> getQuoteList(){
        return retriever.getQuoteList();
    }
}
