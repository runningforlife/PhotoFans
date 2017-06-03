package com.github.runningforlife.photofans.utils;

import android.os.Looper;

/**
 * record the execution time of a thread
 */

public class ThreadUtil {
    private static ThreadLocal<Long> elapse = new ThreadLocal<Long>(){
        @Override
        protected Long initialValue(){
            return System.currentTimeMillis();
        }
    };

    public static void start(){
        elapse.set(System.currentTimeMillis());
    }

    public static long getElapse(){
        return System.currentTimeMillis() - elapse.get();
    }

    public static void checkMain(){
        if(Looper.getMainLooper() == Looper.myLooper()){
            throw new IllegalStateException("this is main thread!!!");
        }
    }
}
