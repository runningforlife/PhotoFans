package com.github.runningforlife.photosniffer.service;

import android.os.HandlerThread;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jason on 6/10/17.
 */

public class LooperThread extends HandlerThread{
    private static AtomicInteger sThreadCount = new AtomicInteger(0);
    private Runnable target;

    public LooperThread(){
        super("LooperThread" + sThreadCount.getAndIncrement());
    }

    public LooperThread(Runnable target){
        super("LooperThread" + sThreadCount.getAndIncrement());
        this.target = target;
    }

    @Override
    public void onLooperPrepared(){
        if(target != null){
            target.run();
        }
    }

}
