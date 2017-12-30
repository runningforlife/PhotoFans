package com.github.runningforlife.photosniffer.crawler.processor;
import java.util.List;

import us.codecraft.webmagic.Page;

/**
 * retrieveImages images from a given page
 */

interface PageRetriever {
    /*
     * retrieveImages images from a given page
     *
     * @param Page: downloaded pages
     * @return List<String> : a list of retrieved image urls
     */
    List<String> retrieveImages(Page page);

    /**
     * retrieveImages page link
     *
     * @param page html page to retrieveImages
     * @return return a list page url
     */
    List<String> retrieveLinks(Page page);
}
