package com.github.runningforlife.photosniffer.ui.adapter;
/**
 * Created by jason on 7/2/17.
 */

public interface PageAdapterCallback extends BaseAdapterCallback,ImageLoadCallback {
    /*
     * item at pos is long clicked
     */
    void onItemLongClicked(int pos, String adapter);
}