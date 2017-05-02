package com.github.runningforlife.photofans.ui;

/**
 * a gallery interface to be used in presenter
 */

public interface GalleryView extends UI {
    void notifyDataChanged();

    void onRefreshDone(boolean isSuccess);
}
