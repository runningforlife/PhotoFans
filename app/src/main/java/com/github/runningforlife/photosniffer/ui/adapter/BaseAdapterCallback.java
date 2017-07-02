package com.github.runningforlife.photosniffer.ui.adapter;

import android.view.View;

import com.github.runningforlife.photosniffer.model.ImageRealm;

/**
 * image detail adapter callback
 */

public interface BaseAdapterCallback {

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
     * @param view - shared element
     */
    void onItemClicked(View view, int pos, String adapter);

    /*
     * remove item at pos
     */
    void removeItemAtPos(int pos);
}
