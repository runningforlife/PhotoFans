package com.github.runningforlife.photosniffer.presenter;

import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;

import io.realm.RealmResults;

/**
 * a presenter to show the detail of the image
 */

public abstract class ImageDetailPresenter implements Presenter,
        RealmManager.UsedDataChangeListener{
    /*
     * favor image at pos
     */
    public abstract void favorImageAtPos(int pos);

    /*
     * set wallpaper
     */
    public abstract void setWallpaper(int pos);

}
