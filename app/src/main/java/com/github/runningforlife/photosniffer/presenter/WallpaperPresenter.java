package com.github.runningforlife.photosniffer.presenter;

import android.util.Log;

import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.ui.UI;

import io.realm.RealmResults;

/**
 * Created by jason on 6/16/17.
 */

public abstract class WallpaperPresenter implements Presenter{

    public void checkNotNull(Object object){
        if(object == null){
            throw new IllegalArgumentException("view should not be null");
        }
    }

    public abstract void refresh();

    @Override
    public void onUnusedDataChange(RealmResults<ImageRealm> data){
        // keep empty
    }

    @Override
    public void onFavorDataChange(RealmResults<ImageRealm> data){
        // keep empty
    }

    @Override
    public void onUsedDataChange(RealmResults<ImageRealm> data) {
        // keep empty
    }
}
