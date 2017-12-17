package com.github.runningforlife.photosniffer.presenter;

/**
 * Created by jason on 12/17/17.
 */

public interface FullScreenPresenter extends LifeCycle {

    void setAsWallpaper(String url);

    void saveImage(String url);
}
