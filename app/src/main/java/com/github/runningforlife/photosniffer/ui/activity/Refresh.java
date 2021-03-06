package com.github.runningforlife.photosniffer.ui.activity;

/**
 * an interface to control refresher in fragment by parent view
 */

public interface Refresh {
    /*
     * whether it is refreshing
     */
    boolean isRefreshing();

    /*
     * change refreshing state
     */
    void setRefreshing(boolean enable);
}
