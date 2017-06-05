package com.github.runningforlife.photosniffer.presenter;

import com.github.runningforlife.photosniffer.model.ImageRealm;

/**
 * a presenter to show the detail of the image
 */

public interface ImageDetailPresenter extends Presenter {

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

    /**
     * remove file at give position
     */
    void removeItemAtPos(int pos);


    /*
     * save image at pos
     */
    void saveImageAtPos(int pos);

    /*
     * favor image at pos
     */
    void favorImageAtPos(int pos);

    /*
     * set wallpaper
     */
    void setWallpaper(int pos);
}
