package com.github.runningforlife.photosniffer.service;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;


/**
 * a result receiver to receiver data from IntentService
 */

public class SimpleResultReceiver extends ResultReceiver{

    private Receiver mReceiver;

    public SimpleResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver){
        mReceiver = receiver;
    }

    public interface Receiver{
        void onReceiveResult(int resultCode, Bundle data);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle data){
        if(mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, data);
        }
    }

}