package com.github.runningforlife.photosniffer.crawler.processor;

/**
 * filter page that we are not interested
 */

public interface PageFilter {

    /*
     * filter the page
     */
    boolean accept(String url);
}
