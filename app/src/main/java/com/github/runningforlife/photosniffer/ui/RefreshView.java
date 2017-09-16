package com.github.runningforlife.photosniffer.ui;

/**
 * Created by jason on 9/16/17.
 */

public interface RefreshView {
    /**
     * data set refresh done
     * @param isSuccess whether action of refreshing is successful
     */
    void onRefreshDone(boolean isSuccess);
}
