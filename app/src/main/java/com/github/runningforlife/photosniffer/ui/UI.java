package com.github.runningforlife.photosniffer.ui;

import com.github.runningforlife.photosniffer.presenter.RealmOp;

/**
 * a UI interface to be used in presenter
 *
 * @author JasonWang
 */

public interface UI {
    /**
     * on data change
     */
    void onDataSetChange(int start, int end, RealmOp op);

    /*
    * notify user the result of setting wallpaper
    */
    void onWallpaperSetDone(boolean isOk);
}
