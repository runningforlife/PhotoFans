package jason.github.com.photofans.ui;

import java.util.List;

import jason.github.com.photofans.model.ImageRealm;

/**
 * a gallery presenter used to load photo list
 */

public interface GalleryPresenter extends Presenter{
    /*
     * load data list synchronously
     */
    List<ImageRealm> loadAllData();

    /*
     * load data asynchronously
     */
    void loadAllDataAsync();

    /*
     * refresh data(download from network asynchrously)
     */
    void refresh();
}
