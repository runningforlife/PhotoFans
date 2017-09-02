package com.github.runningforlife.photosniffer.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.model.QuoteRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.service.QuotesRetrieveService;
import com.github.runningforlife.photosniffer.service.ServiceStatus;
import com.github.runningforlife.photosniffer.service.SimpleResultReceiver;
import com.github.runningforlife.photosniffer.ui.GalleryView;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;

/**
 * Created by jason on 7/18/17.
 */

public class GalleryPresenterImpl implements GalleryPresenter{
    private static final String TAG = "GalleryPresenter";
    private static final int DEFAULT_RETRIEVE_QUOTES = 10;
    private static final int DEFAULT_PULL_TIMEOUT = 3*1000;
    private static long AUTO_REFRESH_INTERVAL = 0;

    private Context context;
    private GalleryView view;
    private RealmManager realmMgr;
    private int current; // current quote to be showed
    private List<QuoteRealm> quotes;
    private SimpleResultReceiver receiver;
    private boolean isRefreshing;
    // last refreshing time
    private long lastRefreshing;
    private H handler;

    public GalleryPresenterImpl(Context context, GalleryView view){
        this.view = view;

        quotes = new ArrayList<>();
        current = -1;

        isRefreshing = false;
        this.context = context;
        receiver = new SimpleResultReceiver(new Handler(Looper.myLooper()));

        String key = context.getString(R.string.last_refreshing_time);
        lastRefreshing = SharedPrefUtil.getLong(key, System.currentTimeMillis());

        AUTO_REFRESH_INTERVAL = context.getResources().
                getInteger(R.integer.auot_quote_refresh_interval);
        handler = new H(Looper.myLooper());
    }

    @Override
    public void init() {
        realmMgr = RealmManager.getInstance();
        realmMgr.onStart();
    }

    @Override
    public void refresh() {
        Log.v(TAG,"refresh()");

        long current = System.currentTimeMillis();
        if(quotes.size() <= 0 || current >= lastRefreshing + AUTO_REFRESH_INTERVAL){
            isRefreshing = true;
            startRetrieveQuote();
            lastRefreshing = System.currentTimeMillis();
            String key = context.getString(R.string.last_refreshing_time);
            SharedPrefUtil.putLong(key, lastRefreshing);
        }else{
            isRefreshing = false;
            view.onRefreshDone(true);
        }
    }

    @Override
    public QuoteRealm getNextQuote() {
        Log.v(TAG,"getNextQuote()");

        ++current;
        current %= quotes.size();

        return quotes.get(current);
    }

    @Override
    public void onQuoteDataChange(RealmResults<QuoteRealm> data) {
        Log.v(TAG,"onQuoteDataChange(): data size = " + data.size());
        if(data != null) {
            for(QuoteRealm item : data){
                if(!quotes.contains(item)){
                    quotes.add(item);
                }
            }
        }

        if(isRefreshing) {
            isRefreshing = false;
            if (quotes.size() <= 0) {
                view.onRefreshDone(false);
            } else {
                view.onRefreshDone(true);
            }
        }
    }

    @Override
    public void onStart() {
        Log.v(TAG, "onStart()");
        realmMgr.addQuoteDataChangeListener(this);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG,"onDestroy()");
        realmMgr.removeQuoteDataChangeListener(this);
        realmMgr.onDestroy();
    }

    private void startRetrieveQuote(){
        Log.v(TAG,"startRetrieveQuote()");
        Intent intent = new Intent(context, QuotesRetrieveService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra(QuotesRetrieveService.EXTRA_RETRIEVE_QUOTES, DEFAULT_RETRIEVE_QUOTES);
        context.startService(intent);

        // timeout event
        handler.sendEmptyMessageAtTime(H.EVENT_PULL_QUOTE_TIMEOUT,
                DEFAULT_PULL_TIMEOUT + SystemClock.currentThreadTimeMillis());
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        Log.v(TAG,"onReceiveResult(): result code = " + resultCode);
        switch (resultCode){
            case ServiceStatus.RUNNING:
                Log.v(TAG,"quotes retrieving started");
                break;
            case ServiceStatus.SUCCESS:
                Log.v(TAG,"quotes retrieving success");
                if(isRefreshing) {
                    view.onRefreshDone(true);
                    isRefreshing = false;
                }
                break;
            case ServiceStatus.ERROR:
                Log.v(TAG,"quotes retrieving fail");
                if(isRefreshing) {
                    isRefreshing = false;
                    if (quotes.size() <= 0) {
                        view.onRefreshDone(false);
                    } else {
                        // we have data to offer
                        view.onRefreshDone(true);
                    }
                }
                break;
            default:
                break;
        }
    }


    private class H extends Handler{

        static final int EVENT_PULL_QUOTE_TIMEOUT = 1;

        H(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case EVENT_PULL_QUOTE_TIMEOUT:
                    if(isRefreshing){
                        isRefreshing = false;
                        view.onRefreshDone(false);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
