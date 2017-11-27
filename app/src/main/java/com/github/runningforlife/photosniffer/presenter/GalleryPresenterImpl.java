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

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by jason on 7/18/17.
 */

public class GalleryPresenterImpl implements GalleryPresenter{
    private static final String TAG = "GalleryPresenter";

    private static final int DEFAULT_RETRIEVE_QUOTES = 10;
    private static final int DEFAULT_PULL_TIMEOUT = 15*1000; //15s
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

        AUTO_REFRESH_INTERVAL = context.getResources().
                getInteger(R.integer.auot_quote_refresh_interval);
        handler = new H(Looper.myLooper());
        receiver = new SimpleResultReceiver(handler);
    }

    @Override
    public void init() {
        realmMgr = RealmManager.getInstance();
        realmMgr.onStart();
    }

    @Override
    public void refresh() {
        Log.v(TAG,"refresh()");
        final long current = System.currentTimeMillis();

        final String key = context.getString(R.string.last_refreshing_time);
        lastRefreshing = SharedPrefUtil.getLong(key, current);
        if(quotes == null ||  quotes.size() <= 0 ||
                current == lastRefreshing || current >= lastRefreshing + AUTO_REFRESH_INTERVAL){
            isRefreshing = true;
            startRetrieveQuote();
            SharedPrefUtil.putLong(key,current);
        }else{
            isRefreshing = false;
            view.onRefreshDone(true);
        }
    }

    @Override
    public QuoteRealm getNextQuote() {
        if(quotes == null || quotes.size() <= 0) return null;

        Log.v(TAG,"getNextQuote()");
        ++current;
        current %= quotes.size();

        Log.d(TAG,"getNextQuote(): quote = " + quotes.get(current));
        return quotes.get(current);
    }

    @Override
    public void favorQuote() {
        Log.v(TAG,"favorQuote()");
        if(quotes != null && quotes.size() > 0) {
            boolean isFavored = quotes.get(current).getIsFavor();
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            QuoteRealm qr = quotes.get(current);
            qr.setIsFavor(!isFavored);

            realm.commitTransaction();
        }
    }

    @Override
    public boolean isCurrentFavored() {
        return current != -1 && quotes.size() > 0
                && quotes.get(current).getIsFavor();
    }

    @Override
    public void onQuoteDataChange(RealmResults<QuoteRealm> data) {
        Log.v(TAG,"onQuoteDataChange(): data size = " + data.size());
        for(QuoteRealm item : data){
            if(!quotes.contains(item)){
                quotes.add(item);
            }
        }

        if (quotes.size() <= 0) {
            view.onRefreshDone(false);
        } else if(isRefreshing){
            view.onRefreshDone(true);
        }

        if(isRefreshing) {
            isRefreshing = false;
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
                int count = data.getInt("result");
                if(count <= 0){
                    view.onRefreshDone(false);
                }else{
                    view.onRefreshDone(true);
                }
                if(isRefreshing) {
                    isRefreshing = false;
                }
                break;
            case ServiceStatus.ERROR:
                Log.v(TAG,"quotes retrieving fail");
                if(isRefreshing) {
                    isRefreshing = false;
                }
                if (quotes.size() <= 0) {
                    view.onRefreshDone(false);
                } else {
                    // we have data to offer
                    view.onRefreshDone(true);
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
                    }
                    if(quotes.size() <= 0){
                        view.onRefreshDone(false);
                    }else{
                        view.onRefreshDone(true);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
