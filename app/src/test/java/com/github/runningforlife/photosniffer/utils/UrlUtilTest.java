package com.github.runningforlife.photosniffer.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by jason on 12/30/17.
 */

public class UrlUtilTest {

    private static final String URL_0 = "https://images.pexels.com/photos/740750/pexels-photo-740750.png?h=350&auto=compress&cs=tinysrgb";
    private static final String URL_1 = "http://ac-pivxf9c9.clouddn.com/YwhCWfBzuOKAljh27dVi1C1ODteYMsPN78AgERNg.txt";
    private static final String URL_2 = "http://online-metrics.com/wp-content/uploads/2015/02/Google-Analytics-API-dimension-filters.png";


    @Test
    public void testIsPossibleImageUrl() {
        assertTrue("image url", UrlUtil.isPossibleImageUrl(URL_0));
        assertTrue("image url", UrlUtil.isPossibleImageUrl(URL_2));
        assertFalse("not image url", UrlUtil.isPossibleImageUrl(URL_1));
    }
}
