package com.github.runningforlife.photosniffer.ui;

/**
 * a UI interface to be used in presenter
 *
 * @author JasonWang
 */

public interface UI {
    /*
     * data set is changed
     */
    void onDataSetChanged();

    /*
     * image save is done
     */
    void onImageSaveDone(String path);

    /*
     * notify user the result of setting wallpaper
     */
    void onWallpaperSetDone(boolean isOk);
}
