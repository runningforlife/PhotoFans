package com.github.runningforlife.photosniffer.crawler.processor;
import java.util.HashMap;
import java.util.List;

import us.codecraft.webmagic.Page;

/**
 * retrieveImages images from a given page
 */

abstract class PageRetriever {
    // attributes to locate image link
    String ATTR_IMAGE_URL = "src";
    String ATTR_REF = "href";
    String ATTR_ALT = "alt";

    String TAG_IMG = "img";
    String TAG_HREF = "a";

    String REG_IMAGE = "img[src=$[.jpeg|.jpg|.png]";


    protected HashMap<String, Boolean> mPageState;

    PageRetriever(HashMap<String,Boolean> pageState) {
        mPageState = pageState;
    }

    /*
     * retrieveImages images from a given page
     *
     * @param Page: downloaded pages
     * @return List<String> : a list of retrieved image urls
     */
    abstract List<String> retrieveImages(Page page);

    /**
     * retrieveImages page link
     *
     * @param page html page to retrieveImages
     * @return return a list page url
     */
    abstract List<String> retrieveLinks(Page page);

    protected boolean isPageRetrieved(String pageUrl) {
        return mPageState.size() != 0 && ((mPageState.containsKey(pageUrl) && mPageState.get(pageUrl)));
    }

}
