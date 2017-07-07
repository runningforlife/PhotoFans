package com.github.runningforlife.photosniffer.presenter;

import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;

import io.realm.RealmResults;

/**
 * presenter to get favor image list
 */

public abstract class FavorImagePresenter implements Presenter, RealmManager.FavorDataChangeListener{

    public abstract void refresh();

    public abstract void cancelFavorAtPos(int pos);

    public abstract void setWallpaperAtPos(int pos);
}
