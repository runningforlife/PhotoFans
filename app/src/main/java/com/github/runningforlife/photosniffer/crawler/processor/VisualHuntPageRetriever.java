package com.github.runningforlife.photosniffer.crawler.processor;

import java.util.List;

import us.codecraft.webmagic.Page;

/**
 * visual hunt page retriever
 */

public class VisualHuntPageRetriever implements PageRetriever {
    private static final String TAG = VisualHuntPageRetriever.class.getSimpleName();

    @Override
    public List<String> retrieveImages(Page page) {
        return null;
    }

    @Override
    public List<String> retrieveLinks(Page page) {
        return null;
    }

}
