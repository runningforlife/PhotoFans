package com.github.runningforlife.photosniffer.presenter;

import com.github.runningforlife.photosniffer.data.local.RealmManager;

/**
 * a presenter to show the detail of the image
 */

public interface ImageDetailPresenter {
    /*
     * favor image at pos
     */
    void favorImageAtPos(int pos);

    /*
     * set wallpaper
     */
    void setWallpaperAtPos(int pos);

}
