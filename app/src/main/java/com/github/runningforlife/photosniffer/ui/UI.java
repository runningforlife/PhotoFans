package com.github.runningforlife.photosniffer.ui;

/**
 * a UI interface to be used in presenter
 *
 * @author JasonWang
 */

public interface UI {
    /**
     * the range of data set is changed
     */
    void onDataSetRangeChange(int start, int count);
}
