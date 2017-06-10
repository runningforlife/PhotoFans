package com.github.runningforlife.photosniffer.presenter;

import com.github.runningforlife.photosniffer.model.ImageRealm;

import io.realm.RealmResults;

/**
 * presenter to get favor image list
 */

public abstract class FavorImagePresenter implements Presenter{


    public abstract void refresh();
    /*
     * cancel favor an image at pos
     */
    public abstract void cancelFavorAtPos(int pos);

    @Override
    public void onUsedRealmDataChange(RealmResults<ImageRealm> data){
        // keep empty
    }

    @Override
    public void onUnusedRealmDataChange(RealmResults<ImageRealm> data){
        // keep empty
    }
}
