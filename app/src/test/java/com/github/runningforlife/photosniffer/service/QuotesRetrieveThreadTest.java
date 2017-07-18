package com.github.runningforlife.photosniffer.service;

import com.github.runningforlife.photosniffer.crawler.processor.QuotePageProcessor;
import com.github.runningforlife.photosniffer.crawler.processor.QuotesRetriever;

import org.junit.Before;
import org.junit.Test;

import us.codecraft.webmagic.processor.PageProcessor;

import static org.junit.Assert.*;

/**
 * Created by jason on 7/18/17.
 */

public class QuotesRetrieveThreadTest {
    private QuotesRetrieveThread thread;
    private QuotePageProcessor processor;

    @Before
    public void setUp(){
        processor = new QuotePageProcessor(10);
        thread = new QuotesRetrieveThread(10);
        thread.setProcessor(processor);
    }

    @Test
    public void testCrawler(){
        QuotesRetrieveThread.QuoteRetrieveCallback callback =
                new QuotesRetrieveThread.QuoteRetrieveCallback() {
                    @Override
                    public void onRetrieveComplete(boolean success) {
                        assertEquals(true, success);
                        assertNotEquals(null, processor.getPageList());
                        assertNotEquals(null, processor.getQuoteList());

                        assertNotEquals(0, processor.getPageList().size());
                        assertNotEquals(0, processor.getQuoteList().size());
                    }
                };
        thread.setCallback(callback);
        thread.start();
    }
}
