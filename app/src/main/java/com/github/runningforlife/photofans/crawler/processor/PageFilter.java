package com.github.runningforlife.photofans.crawler.processor;

/**
 * filter page that we are not interested
 */

public interface PageFilter {

    /*
     * filter the page
     */
    boolean accept(String url);
}
