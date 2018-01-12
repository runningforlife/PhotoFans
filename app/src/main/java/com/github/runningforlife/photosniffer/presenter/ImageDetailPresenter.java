package com.github.runningforlife.photosniffer.presenter;

/**
 * a presenter to show the detail of the image
 */

public interface ImageDetailPresenter {
    /*
     * favor image at pos
     */
    void favorImageAtPos(int pos);

    /**
     * change image type to switch image lsit
     */
    void onImageTypeChange(int type);
}
