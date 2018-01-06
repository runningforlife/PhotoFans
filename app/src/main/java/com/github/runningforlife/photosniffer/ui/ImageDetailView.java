package com.github.runningforlife.photosniffer.ui;

/**
 * Created by jason on 4/6/17.
 */

public interface ImageDetailView extends UI {
    /*
    * image start loading
    */
    void onImageLoadStart(int pos);

    /*
     * image loading complete
     */
    void onImageLoadDone(int pos, boolean isSuccess);
}
