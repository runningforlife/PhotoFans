package com.github.runningforlife.photosniffer.presenter;

import com.github.runningforlife.photosniffer.data.local.RealmManager;

/**
 * presenter to get favor image list
 */

public interface FavorImagePresenter extends Presenter, RealmManager.FavorDataChangeListener{

    void refresh();

    void setWallpaperAtPos(int pos);
}
