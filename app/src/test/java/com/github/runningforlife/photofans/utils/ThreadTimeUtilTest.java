package com.github.runningforlife.photofans.utils;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;

/**
 * test thread time utility class
 */

public class ThreadTimeUtilTest{

    @Test
    public void threadElapseTest(){
        ThreadTimeUtil.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                assertEquals("time should larger than 3s", ThreadTimeUtil.getElapse()/1000 >= 3, true);
            }
        }).start();
    }
}
