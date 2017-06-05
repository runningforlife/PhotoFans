package com.github.runningforlife.photosniffer.presenter;

import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.utils.DisplayUtil;

/**
 * a presenter to do interactions with UI and update database
 *
 * @author JasonWang
 * @since 1.0
 */

public interface Presenter extends RealmManager.RealmDataChangeListener,
        ImageSaveRunnable.ImageSaveCallback, LifeCycle {
    int DEFAULT_WIDTH = 1024;
    int DEFAULT_HEIGHT = (int)(DEFAULT_WIDTH* DisplayUtil.getScreenRatio());

    /*
     * init presenter
     */
    void init();
}
