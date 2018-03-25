package com.github.runningforlife.photosniffer.presenter;

/**
 * a gallery presenter used to load photo list
 */

public interface AllPicturesPresenter {

    void refresh();

    void refreshAnyway();

    void favorImageAtPos(int pos);
}
