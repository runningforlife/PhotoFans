package com.github.runningforlife.photosniffer.presenter;

import com.github.runningforlife.photosniffer.model.ImageRealm;

import io.realm.RealmResults;

/**
 * a gallery presenter used to load photo list
 */

public abstract class GalleryPresenter implements Presenter {

    /*
     * refresh data(download from network asynchrously)
     */
    public abstract void refresh();

    public abstract void refreshAnyway();

    @Override
    public void onFavorDataChange(RealmResults<ImageRealm> data){
        // keep empty
    }


    @Override
    public void onWallpaperDataChange(RealmResults<ImageRealm> data) {
        // not care
    }
}
