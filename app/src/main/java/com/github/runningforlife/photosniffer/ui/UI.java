package com.github.runningforlife.photosniffer.ui;

import com.github.runningforlife.photosniffer.presenter.NetState;
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
    * image save is done
    */
    void onImageSaveDone(String path);

    /*
    * notify user the result of setting wallpaper
    */
    void onWallpaperSetDone(boolean isOk);

    /**
     * notify user we are busy for removing/saving
     */
    void notifyJobState(boolean isStarted, String hint);
}
