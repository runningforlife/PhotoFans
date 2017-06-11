package com.github.runningforlife.photosniffer.ui;

/**
 * a gallery interface to be used in presenter
 */

public interface GalleryView extends UI {
    void notifyDataChanged();

    void onRefreshDone(boolean isSuccess);

    void onNetworkDisconnect();

    void onMobileConnected();
}
