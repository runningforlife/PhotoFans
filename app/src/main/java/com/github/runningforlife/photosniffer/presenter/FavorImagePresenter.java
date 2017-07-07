package com.github.runningforlife.photosniffer.presenter;

import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;

import io.realm.RealmResults;

/**
 * presenter to get favor image list
 */

public interface FavorImagePresenter extends Presenter, RealmManager.FavorDataChangeListener{

    void refresh();

    void setWallpaperAtPos(int pos);
}
