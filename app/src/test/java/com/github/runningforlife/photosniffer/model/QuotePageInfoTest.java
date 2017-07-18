package com.github.runningforlife.photosniffer.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jason on 7/18/17.
 */

public class QuotePageInfoTest {
    private static final String QUOTE_SOURCE = "http://www.quotery.com";

    private QuotePageInfo page1;
    private QuotePageInfo page2;
    private QuotePageInfo page3;

    @Before
    public void setUp(){
        page1 = new QuotePageInfo(QUOTE_SOURCE, false);
        page2 = new QuotePageInfo(QUOTE_SOURCE, true);
        page3 = new QuotePageInfo();
        page3.setUrl("www.google.com");
    }

    @Test
    public void testQuotePageEquals(){
        assertNotEquals(true, page1.equals(null));
        assertNotEquals(true , page1.equals(new Object()));
        assertNotEquals(false, page1.equals(page2));
        assertNotEquals(true, page1.equals(page3));
    }
}
