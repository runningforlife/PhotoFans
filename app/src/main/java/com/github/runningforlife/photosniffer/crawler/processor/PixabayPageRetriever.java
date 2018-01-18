package com.github.runningforlife.photosniffer.crawler.processor;

import java.util.HashMap;
import java.util.List;

import us.codecraft.webmagic.Page;

/**
 * pixabay page retriever
 */

public class PixabayPageRetriever implements PageRetriever {
    private static final String TAG = PixabayPageRetriever.class.getSimpleName();

    private HashMap<String, Boolean> mPageState;

    PixabayPageRetriever(HashMap<String, Boolean> pageState) {
        mPageState = pageState;
    }

    @Override
    public List<String> retrieveImages(Page page) {
        return null;
    }

    @Override
    public List<String> retrieveLinks(Page page) {
        return null;
    }
}
