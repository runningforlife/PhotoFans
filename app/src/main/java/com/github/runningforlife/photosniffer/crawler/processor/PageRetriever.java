package com.github.runningforlife.photosniffer.crawler.processor;

import com.github.runningforlife.photosniffer.data.model.ImagePageInfo;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;

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
    List<ImageRealm> retrieveImages(Page page);

    /**
     * retrieveImages page link
     *
     * @param page html page to retrieveImages
     * @return return a list page url
     */
    List<ImagePageInfo> retrieveLinks(Page page);
}
