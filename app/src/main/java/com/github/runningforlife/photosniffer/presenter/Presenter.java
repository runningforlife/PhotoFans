package com.github.runningforlife.photosniffer.presenter;

import android.util.DisplayMetrics;

import com.github.runningforlife.photosniffer.utils.DisplayUtil;

import io.realm.RealmObject;

/**
 * a presenter to do interactions with UI and update database
 *
 * @author JasonWang
 * @since 1.0
 */

interface Presenter extends ImageSaveRunnable.ImageSaveCallback, LifeCycle {
    int DEFAULT_WIDTH = 1024;
    int DEFAULT_HEIGHT = (int)(DEFAULT_WIDTH* DisplayUtil.getScreenRatio());

    DisplayMetrics dm = DisplayUtil.getScreenDimen();

    /*
     * init presenter
     */
    void init();


    /*
     * get total item
     */
    int getItemCount();

    /*
     * get item at given position
     */
    RealmObject getItemAtPos(int pos);

    /*
     * remove item at given position
     */
    void removeItemAtPos(int pos);

    /*
     * save bitmap at pos
     */
    void saveImageAtPos(int pos);
}
