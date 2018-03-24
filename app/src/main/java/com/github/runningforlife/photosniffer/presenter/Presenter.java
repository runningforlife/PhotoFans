package com.github.runningforlife.photosniffer.presenter;

import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.ui.fragment.BatchAction;
import com.github.runningforlife.photosniffer.utils.DisplayUtil;

import java.util.List;

import io.realm.RealmObject;

/**
 * a presenter to do interactions with UI and updateAsync database
 *
 * @author JasonWang
 * @since 1.0
 */

public interface Presenter extends LifeCycle {
    /** event */
    int EVENT_BATCH_REMOVE_TIMEOUT = 0x01;
    int EVENT_BATCH_SAVE_TIMEOUT = 0x02;
    int EVENT_IMAGE_LOAD_DONE = 0x03;
    int EVENT_WALLPAPER_SET_DONE = 0x04;
    int EVENT_SAVE_AS_WALLPAPER = 0x05;

    String EXTRA_WALLPAPER_URL = "wallpaper_url";
    String EXTRA_WALLPAPER_POSITION = "wallpaper_position";

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
    void onImageLoadDone(boolean isSuccess);

    /**
     * save user selected photos to realm
     */
    void saveUserPickedPhotos(List<String> photoUris);

    /**
     *  batch edit
     */
    void batchEdit(List<String> images, @BatchAction String action);
}
