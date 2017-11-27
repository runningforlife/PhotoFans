package com.github.runningforlife.photosniffer.presenter;

import com.github.runningforlife.photosniffer.data.local.RealmManager;
import com.github.runningforlife.photosniffer.ui.WallpaperView;

/**
 * Created by jason on 6/16/17.
 */

public interface WallpaperPresenter extends Presenter,
        RealmManager.WallpaperDataChangeListener{

    void refresh();

    void setWallpaperAtPos(int pos);

    void setView(WallpaperView view);

}
