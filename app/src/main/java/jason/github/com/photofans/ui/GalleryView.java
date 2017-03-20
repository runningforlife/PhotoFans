package jason.github.com.photofans.ui;

import java.util.List;

import jason.github.com.photofans.model.ImageRealm;

/**
 * a gallery interface to be used in presenter
 */

public interface GalleryView extends UI {
    void notifyDataChanged(List<ImageRealm> result);

    void onRefreshDone(boolean isSuccess);
}
