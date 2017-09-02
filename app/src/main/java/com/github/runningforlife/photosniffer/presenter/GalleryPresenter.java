package com.github.runningforlife.photosniffer.presenter;

import android.support.v4.os.ResultReceiver;

import com.github.runningforlife.photosniffer.model.QuoteRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.service.QuotesRetrieveService;
import com.github.runningforlife.photosniffer.service.SimpleResultReceiver;

/**
 *  a presenter to get data for gallery activity
 */

public interface GalleryPresenter extends RealmManager.QuoteDataChangeListener,
        LifeCycle, SimpleResultReceiver.Receiver{
    void init();

    void refresh();

    QuoteRealm getNextQuote();
}
