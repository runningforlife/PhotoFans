package com.github.runningforlife.photosniffer.presenter;

import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.utils.DisplayUtil;

import io.realm.RealmObject;

/**
 * a presenter to do interactions with UI and updateAsync database
 *
 * @author JasonWang
 * @since 1.0
 */

public interface Presenter extends ImageSaveRunnable.ImageSaveCallback, LifeCycle {
    int DEFAULT_WIDTH = 1024;
    int DEFAULT_HEIGHT = (int)(DEFAULT_WIDTH* DisplayUtil.getScreenRatio());
    // if network error count is larger than 10, network is bad
    int NETWORK_HUNG_ERROR_COUNT = 10;
    int NETWORK_SLOW_ERROR_COUNT = 5;

    DisplayMetrics dm = DisplayUtil.getScreenDimen();

    /*
     * get total item
     */
    int getItemCount();

    /*
     * get item at given position
     */
    RealmObject getItemAtPos(int pos);

    /*
     * remove item at given position
     */
    void removeItemAtPos(int pos);

    /*
     * save bitmap at pos
     */
    void saveImageAtPos(int pos);

    /**
     * set wallpaper at pos
     */
    void setWallpaperAtPos(int pos);

    /**
     * load image into view
     */
    void loadImageIntoView(int pos, ImageView iv, Priority priority, int w, int h, ImageView.ScaleType scaleType);

    /*
    * image start loading
    */
    void onImageLoadStart(int pos);

    /*
     * image loading complete
     */
    void onImageLoadDone(int pos, boolean isSuccess);
}
