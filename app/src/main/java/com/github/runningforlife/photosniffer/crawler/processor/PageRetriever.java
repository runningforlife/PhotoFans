package com.github.runningforlife.photosniffer.crawler.processor;

import java.util.List;

import us.codecraft.webmagic.Page;

/**
 * retrieve images from a given page
 */

public interface PageRetriever<T> {
    /*
     * retrieve images from a given page
     *
     * @param Page: downloaded pages
     * @return List<String> : a list of retrieved image urls
     */
    List<T> retrieve(Page page);
}
