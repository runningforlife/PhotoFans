package com.github.runningforlife.photosniffer.presenter;

import android.webkit.WebView;

import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;

import io.realm.RealmResults;

/**
 * a gallery presenter used to load photo list
 */

public interface AllPicturesPresenter extends Presenter,
        RealmManager.UsedDataChangeListener, RealmManager.UnusedDataChangeListener {
    /*
     * refresh data(download from network asynchrously)
     */
    void refresh();

    void refreshAnyway();

    void setWebView(WebView webView);

    void setWallpaperAtPos(int pos);

    void favorImageAtPos(int pos);
}
