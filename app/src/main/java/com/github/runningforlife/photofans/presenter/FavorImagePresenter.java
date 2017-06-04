package com.github.runningforlife.photofans.presenter;

import com.github.runningforlife.photofans.model.ImageRealm;

/**
 * presenter to get favor image list
 */

public interface FavorImagePresenter extends Presenter{
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
    void saveImageAtPos(int pos);

    /*
     * cancel favor an image at pos
     */
    void cancelFavorAtPos(int pos);
}
