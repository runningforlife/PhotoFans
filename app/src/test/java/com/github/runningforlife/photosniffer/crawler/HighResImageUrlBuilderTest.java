package com.github.runningforlife.photosniffer.crawler;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;


/**
 * Created by jason on 1/19/18.
 */

public class HighResImageUrlBuilderTest {

    @Test
    public void FreeJpgUrlBuilderTest() {
        String hres = HighResImageUrlBuilder.buildHighResImageUrl("http://en.freejpg.com.ar/asset/400/95/958c/F100007404.jpg");

        assertEquals(hres, "http://en.freejpg.com.ar/asset/900/95/958c/F100007404.jpg");
    }
}
