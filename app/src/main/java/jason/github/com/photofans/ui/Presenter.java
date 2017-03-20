package jason.github.com.photofans.ui;

import jason.github.com.photofans.repository.RealmHelper;

/**
 * a presenter to do interactions with UI and update database
 *
 * @author JasonWang
 * @since 1.0
 */

public interface Presenter extends RealmHelper.RealmDataChangeListener{
    void init();

    void onDestroy();
}
