package com.github.runningforlife.photosniffer.ui;

/**
 * a gallery interface to be used in presenter
 */

public interface AllPictureView extends UI, RefreshView {

    void onNetworkDisconnect();

    void onMobileConnected();
}
