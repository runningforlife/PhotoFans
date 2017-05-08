package com.github.runningforlife.photofans.ui;

import com.github.runningforlife.photofans.realm.ImageRealm;

/**
 * a presenter to show the detail of the image
 */

public interface ImageDetailPresenter extends Presenter{

    /*
     * get image item at position
     * @param pos - the item position
     * @return ImageRealm - a realm object
     */
    ImageRealm getItemAtPos(int pos);

    /*
     * get the count of all items
     * @return the number of all items
     */
    int getItemCount();
}
