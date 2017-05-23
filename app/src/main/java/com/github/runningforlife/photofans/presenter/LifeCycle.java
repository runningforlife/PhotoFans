package com.github.runningforlife.photofans.presenter;

/**
 *  interface to follow activity/fragment lifecycle
 */

public interface LifeCycle {
    /**
     * activity is started
     */
    void onStart();

    /**
     * activity is destroyed
     */
    void onDestroy();
}
