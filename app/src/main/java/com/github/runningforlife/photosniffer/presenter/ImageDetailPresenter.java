package com.github.runningforlife.photosniffer.presenter;

import com.github.runningforlife.photosniffer.model.ImageRealm;

import io.realm.RealmResults;

/**
 * a presenter to show the detail of the image
 */

public abstract class ImageDetailPresenter implements Presenter {
    /*
     * favor image at pos
     */
    public abstract void favorImageAtPos(int pos);

    /*
     * set wallpaper
     */
    public abstract void setWallpaper(int pos);

    @Override
    public void onUnusedDataChange(RealmResults<ImageRealm> data){
        // keep empty
    }

    @Override
    public void onFavorDataChange(RealmResults<ImageRealm> data){
        // keep empty
    }


    @Override
    public void onWallpaperDataChange(RealmResults<ImageRealm> data) {
        // not care
    }
}
