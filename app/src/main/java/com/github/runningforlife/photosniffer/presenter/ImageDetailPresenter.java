package com.github.runningforlife.photosniffer.presenter;

import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;

import io.realm.RealmResults;

/**
 * a presenter to show the detail of the image
 */

public interface ImageDetailPresenter extends Presenter,
        RealmManager.UsedDataChangeListener{
    /*
     * favor image at pos
     */
    void favorImageAtPos(int pos);

    /*
     * set wallpaper
     */
    void setWallpaper(int pos);

}
