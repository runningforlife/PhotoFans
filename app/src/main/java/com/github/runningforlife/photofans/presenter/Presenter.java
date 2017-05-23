package com.github.runningforlife.photofans.presenter;

import com.github.runningforlife.photofans.presenter.LifeCycle;
import com.github.runningforlife.photofans.realm.RealmManager;

/**
 * a presenter to do interactions with UI and update database
 *
 * @author JasonWang
 * @since 1.0
 */

public interface Presenter extends RealmManager.RealmDataChangeListener,LifeCycle {
    /*
     * init presenter
     */
    void init();
}
