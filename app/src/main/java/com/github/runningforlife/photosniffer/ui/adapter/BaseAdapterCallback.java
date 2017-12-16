package com.github.runningforlife.photosniffer.ui.adapter;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Priority;

import io.realm.RealmObject;

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
    RealmObject getItemAtPos(int pos);

    /*
     * item at pos is clicked
     * @param view - shared element
     */
    void onItemClicked(View view, int pos, String adapter);

    /*
     * remove item at pos
     */
    void removeItemAtPos(int pos);

    /**
     * load image into view
     */
    void loadImageIntoView(int pos, ImageView iv, Priority priority, int w, int h, ImageView.ScaleType scaleType);
}
