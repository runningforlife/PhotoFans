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

    @Test
    public void pixabayUrlBuilderTest() {
        String hRes = HighResImageUrlBuilder.buildHighResImageUrl("https://cdn.pixabay.com/photo/2018/01/13/22/14/peacock-3080897__340.jpg");

        assertEquals(hRes, "https://cdn.pixabay.com/photo/2018/01/13/22/14/peacock-3080897_960_720.jpg");
    }

    @Test
    public void visualHuntUrlBuilderTest() {
        String hres = HighResImageUrlBuilder.buildHighResImageUrl("https://visualhunt.com/photos/s/7/night-night-sky-nightsky.jpg");

        assertEquals(hres, "https://visualhunt.com/photos/l/7/night-night-sky-nightsky.jpg");
    }
}
