package com.github.runningforlife.photofans.utils;

/**
 * record the execution time of a thread
 */

public class ThreadTimeUtil {
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
}
