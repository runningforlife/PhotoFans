package com.github.runningforlife.photosniffer.ui;

/**
 * UI interface to be used update data
 */

public interface WallpaperView extends UI{
    void onDataSetChanged();

    void onWallpaperSetDone(boolean isOk);
}
