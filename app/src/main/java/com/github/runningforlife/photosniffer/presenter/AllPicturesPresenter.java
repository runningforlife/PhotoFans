package com.github.runningforlife.photosniffer.presenter;

import android.webkit.WebView;

import com.github.runningforlife.photosniffer.data.local.RealmManager;

/**
 * a gallery presenter used to load photo list
 */

public interface AllPicturesPresenter {
    /*
     * refresh data(download from network asynchrously)
     */
    void refresh();

    void refreshAnyway();

    void setWebView(WebView webView);

    void favorImageAtPos(int pos);
}
