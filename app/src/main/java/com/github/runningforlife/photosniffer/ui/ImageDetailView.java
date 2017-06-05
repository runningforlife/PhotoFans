package com.github.runningforlife.photosniffer.ui;

/**
 * Created by jason on 4/6/17.
 */

public interface ImageDetailView extends UI{

    /*
     * on data set changed,notify UI to change
     */
    void onDataSetChanged();


    /*
     * notify user the result of setting wallpaper
     */
    void onWallpaperSetDone(boolean isOk);
}
