package com.github.runningforlife.photosniffer.ui;

/**
 *  UI interface of quotes view
 */

public interface QuoteView extends UI, RefreshView {
    /**
     * network is disconnected
     */
    void onNetworkDisconnect();
}
