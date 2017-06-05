package com.github.runningforlife.photosniffer.ui.fragment;

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
