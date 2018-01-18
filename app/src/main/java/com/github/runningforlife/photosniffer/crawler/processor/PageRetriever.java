package com.github.runningforlife.photosniffer.crawler.processor;
import java.util.List;

import us.codecraft.webmagic.Page;

/**
 * retrieveImages images from a given page
 */

interface PageRetriever {
    // attributes to locate image link
    String ATTR_IMAGE_URL = "src";
    String ATTR_REF = "href";
    String ATTR_ALT = "alt";

    String TAG_IMG = "img";
    String TAG_HREF = "a";

    String REG_IMAGE = "img[src=$[.jpeg|.jpg|.png]";


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
