package com.github.runningforlife.photosniffer.ui;

/**
 * Created by jason on 6/4/17.
 */

public interface FavorView extends UI{
    /*
     * data set is changed
     */
    void onDataSetChanged();

    /*
     * refresh data set
     */
    void onRefreshDone(boolean isSuccess);
}
