package com.github.runningforlife.photosniffer.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * test QuoteReam
 */

public class QuoteRealmTest {
    private static final String QUOTE_SOURCE = "http://www.quotery.com";

    private QuoteRealm quote1;
    private QuoteRealm quote2;
    private QuoteRealm quote3;
    private QuoteRealm quote4;

    @Before
    public void setUp(){
        quote1 = new QuoteRealm(QUOTE_SOURCE, "Bob", "bob has a lamb");
        quote2 = new QuoteRealm(QUOTE_SOURCE, "bob", "bob has a cake");
        quote3 = new QuoteRealm();
        quote3.setUrl(QUOTE_SOURCE);
        quote4 = quote1;
    }

    @Test
    public void testQuoteReamEquals(){
        assertNotEquals(true, quote1.equals(quote2));
        assertNotEquals(true, quote2.equals(quote3));
        assertNotEquals(true, quote1.equals(new Object()));
        assertNotEquals(true, quote1.equals(null));
        assertEquals(true, quote1.equals(quote4));
    }
}
