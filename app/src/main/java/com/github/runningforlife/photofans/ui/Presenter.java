package com.github.runningforlife.photofans.ui;

import com.github.runningforlife.photofans.model.RealmHelper;

/**
 * a presenter to do interactions with UI and update database
 *
 * @author JasonWang
 * @since 1.0
 */

public interface Presenter extends RealmHelper.RealmDataChangeListener {
    void init();

    void onStart();

    void onDestroy();
}
