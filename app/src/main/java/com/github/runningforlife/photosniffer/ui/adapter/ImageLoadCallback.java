package com.github.runningforlife.photosniffer.ui.adapter;

/**
 * image loading progress call back
 */

interface ImageLoadCallback extends NetworkStateCallback {
    /*
     * image start loading
     */
    void onImageLoadStart(int pos);

    /*
     * image loading complete
     */
    void onImageLoadDone(int pos, boolean isSuccess);
}
