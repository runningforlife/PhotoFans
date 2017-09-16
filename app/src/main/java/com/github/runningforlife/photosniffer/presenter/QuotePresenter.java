package com.github.runningforlife.photosniffer.presenter;

import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.service.SimpleResultReceiver;

/**
 *  presenter to get quotes
 */

public interface QuotePresenter extends Presenter,
        RealmManager.QuoteDataChangeListener, SimpleResultReceiver.Receiver{
    /**
     * favor quotes at position
     */
    void favorQuote(int pos);

    /**
     * retrieve quotes from network
     */
    void refresh();
}
