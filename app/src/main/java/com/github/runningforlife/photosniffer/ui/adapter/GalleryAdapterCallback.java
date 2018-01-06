package com.github.runningforlife.photosniffer.ui.adapter;

/**
 * Created by jason on 7/2/17.
 */

public interface GalleryAdapterCallback extends BaseAdapterCallback {
    /*
     * context menu is created
     */
    void onContextMenuCreated(int pos, String adapter);


    /**
     * item selected
     */
    void onItemSelected(int totalCount);
}
