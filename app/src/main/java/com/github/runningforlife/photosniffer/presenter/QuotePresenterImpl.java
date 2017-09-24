package com.github.runningforlife.photosniffer.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.runningforlife.photosniffer.model.QuoteRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.service.QuotesRetrieveService;
import com.github.runningforlife.photosniffer.service.ServiceStatus;
import com.github.runningforlife.photosniffer.service.SimpleResultReceiver;
import com.github.runningforlife.photosniffer.ui.QuoteView;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by jason on 9/16/17.
 */

public class QuotePresenterImpl implements QuotePresenter {
    private static final String TAG = "QuotesPresenter";

    private static final int DEFAULT_RETRIEVE_QUOTES = 10;
    private static final int DEFAULT_RETRIEVE_TIMEOUT = 22*1000;

    private Context context;
    private QuoteView view;
    private RealmManager realmMgr;
    private RealmResults<QuoteRealm> quotes;
    private SimpleResultReceiver receiver;
    private boolean isRefreshing;
    private Handler handler;

    public QuotePresenterImpl(Context context, @Nullable QuoteView view){
        this.context = context;
        this.view = view;
        realmMgr = RealmManager.getInstance();
    }

    @Override
    public void init() {
        Log.v(TAG,"init()");

        realmMgr.addQuoteDataChangeListener(this);

        handler = new H();
        receiver = new SimpleResultReceiver(handler);
        receiver.setReceiver(this);

        isRefreshing = false;
    }

    @Override
    public int getItemCount() {
        return quotes.size();
    }

    @Override
    public RealmObject getItemAtPos(int pos) {
        return quotes.get(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        Log.v(TAG,"removeItemAtPos()");

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        QuoteRealm qr = quotes.get(pos);
        qr.deleteFromRealm();

        realm.commitTransaction();
    }

    @Override
    public void saveImageAtPos(int pos) {
        // empty
    }

    @Override
    public void onImageSaveDone(String path) {
        // empty
    }

    @Override
    public void onStart() {
        Log.v(TAG,"onStart()");
        realmMgr.onStart();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG,"onDestroy()");
        realmMgr.onDestroy();
    }

    @Override
    public void onQuoteDataChange(RealmResults<QuoteRealm> data) {
        Log.v(TAG,"onQuoteDataChange()");
        quotes = data;
        quotes.sort("savedTime", Sort.DESCENDING);
        if(view != null){
            view.onDataSetChanged();
        }
    }

    @Override
    public void favorQuote(int pos) {
        Log.v(TAG,"favorQuote()");

        Realm rm = Realm.getDefaultInstance();
        rm.beginTransaction();

        QuoteRealm qr = quotes.get(pos);
        qr.setIsFavor(true);

        rm.commitTransaction();
    }

    @Override
    public void refresh() {
        Log.v(TAG,"refresh()");

        if(!MiscUtil.isConnected(context)){
            view.onNetworkDisconnect();
            return;
        }

        if(!isRefreshing) {
            retrieveQuotes();
            isRefreshing = true;
        }
    }

    private void retrieveQuotes(){
        Intent intent = new Intent(context, QuotesRetrieveService.class);
        intent.putExtra("receiver", receiver);
        intent.putExtra(QuotesRetrieveService.EXTRA_RETRIEVE_QUOTES, DEFAULT_RETRIEVE_QUOTES);

        context.startService(intent);
        // timeout message
        Message msg = handler.obtainMessage(H.EVENT_RETRIEVE_QUOTE_TIMEOUT);
        handler.sendMessageDelayed(msg, DEFAULT_RETRIEVE_TIMEOUT);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        Log.v(TAG,"onReceiveResult()");

        switch (resultCode){
            case ServiceStatus.RUNNING:
                Log.v(TAG,"starting retrieving quotes");
                break;
            case ServiceStatus.SUCCESS:
                if(isRefreshing){
                    isRefreshing = false;
                    int count = data.getInt("result");
                    if(count > 0){
                        view.onRefreshDone(true);
                    }else{
                        view.onRefreshDone(false);
                    }
                }
                break;
            case ServiceStatus.ERROR:
                if(isRefreshing){
                    isRefreshing = false;
                    view.onRefreshDone(false);
                }
                break;
            default:
                break;
        }
    }

    private final class H extends  Handler{
        static final int EVENT_RETRIEVE_QUOTE_TIMEOUT = 1;

        H(){
            super(Looper.myLooper());
        }

        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case EVENT_RETRIEVE_QUOTE_TIMEOUT:
                    if(isRefreshing){
                        isRefreshing = false;
                        view.onRefreshDone(false);
                    }
                    break;
            }
        }
    }
}
