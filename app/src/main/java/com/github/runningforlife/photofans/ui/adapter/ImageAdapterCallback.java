package com.github.runningforlife.photofans.ui.adapter;

import com.github.runningforlife.photofans.realm.ImageRealm;

/**
 * image detail adapter callback
 */

public interface ImageAdapterCallback {

    /*
     * get the number of images
     */
    int getCount();

    /*
     * get item at given position
     */
    ImageRealm getItemAtPos(int pos);

    /*
     * item at pos is clicked
     */
    void onItemClicked(int pos, String adapter);


    /*
     * image start loading
     */
    void onImageLoadStart(int pos);

    /*
     * image loading complete
     */
    void onImageLoadDone(int pos, boolean isSuccess);

}
