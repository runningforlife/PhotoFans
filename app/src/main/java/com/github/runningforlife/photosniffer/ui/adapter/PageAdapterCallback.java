package com.github.runningforlife.photosniffer.ui.adapter;

import com.github.runningforlife.photosniffer.ui.adapter.BaseAdapterCallback;

/**
 * Created by jason on 7/2/17.
 */

public interface PageAdapterCallback extends BaseAdapterCallback {
    /*
     * item at pos is long clicked
     */
    void onItemLongClicked(int pos, String adapter);
}
