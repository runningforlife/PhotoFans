package com.github.runningforlife.photosniffer.presenter;

import android.webkit.WebView;

import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;

import io.realm.RealmResults;

/**
 * a gallery presenter used to load photo list
 */

public abstract class GalleryPresenter implements Presenter,
        RealmManager.UsedDataChangeListener, RealmManager.UnusedDataChangeListener {

    /*
     * refresh data(download from network asynchrously)
     */
    public abstract void refresh();

    public abstract void refreshAnyway();

    public abstract void setWebView(WebView webView);

    public abstract void setWallpaperAtPos(int pos);

}
