package com.github.runningforlife.photosniffer.utils;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;

/**
 * test thread time utility class
 */

public class ThreadTimeUtilTest{

    @Test
    public void threadElapseTest(){
        ThreadUtil.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                assertEquals("time should larger than 3s", ThreadUtil.getElapse()/1000 >= 3, true);
            }
        }).start();
    }
}
