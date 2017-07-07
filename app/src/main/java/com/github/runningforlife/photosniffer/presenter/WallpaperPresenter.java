package com.github.runningforlife.photosniffer.presenter;

import android.util.Log;

import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.ui.UI;
import com.github.runningforlife.photosniffer.ui.WallpaperView;

import io.realm.RealmResults;

/**
 * Created by jason on 6/16/17.
 */

public interface WallpaperPresenter extends Presenter,
        RealmManager.WallpaperDataChangeListener{

    void refresh();

    void setWallpaperAtPos(int pos);

    void setView(WallpaperView view);

}
