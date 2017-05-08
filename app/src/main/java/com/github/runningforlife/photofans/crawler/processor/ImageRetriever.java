package com.github.runningforlife.photofans.crawler.processor;

import java.util.List;

import com.github.runningforlife.photofans.realm.ImageRealm;
import us.codecraft.webmagic.Page;

/**
 * retrieve images from a given page
 */

public interface ImageRetriever {
    /*
     * retrieve images from a given page
     *
     * @param Page: downloaded pages
     * @return List<String> : a list of retrieved image urls
     */
    List<ImageRealm> retrieveImages(Page page);
}
