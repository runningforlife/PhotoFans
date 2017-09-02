package com.github.runningforlife.photosniffer.crawler.processor;

/**
 * filter quotes page url
 */

public class QuotePageFilter implements PageFilter {
    public static final String QUOTE_SOURCE_0 = "http://www.quotery.com/";
    //static final String QUOTE_SOURCE_1 = "https://www.brainyquote.com/";

    @Override
    public boolean accept(String url) {
        return url.startsWith(QUOTE_SOURCE_0) || url.contains(QUOTE_SOURCE_0);
    }
}
