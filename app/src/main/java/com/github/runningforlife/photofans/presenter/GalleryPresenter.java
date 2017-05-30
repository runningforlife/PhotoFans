package com.github.runningforlife.photofans.presenter;

import android.graphics.Bitmap;

import com.github.runningforlife.photofans.model.ImageRealm;

/**
 * a gallery presenter used to load photo list
 */

public interface GalleryPresenter extends Presenter {
    /*
     * load data asynchronously
     */
    void loadAllDataAsync();

    /*
     * refresh data(download from network asynchrously)
     */
    void refresh();

    /*
     * get total item
     */
    int getItemCount();

    /*
     * get item at given position
     */
    ImageRealm getItemAtPos(int pos);

    /*
     * remove item at given position
     */
    void removeItemAtPos(int pos);

    /*
     * save bitmap at pos
     */
    void saveImageAtPos(int pos, Bitmap bitmap);
}
