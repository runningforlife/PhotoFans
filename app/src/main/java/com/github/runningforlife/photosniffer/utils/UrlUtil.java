package com.github.runningforlife.photosniffer.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * an utility class to process URLs
 */

public class UrlUtil {

    // get a root url of the give url
    public static String getRootUrl(String url) throws MalformedURLException{
        URL absUrl = new URL(url);
        String authority = absUrl.getAuthority();
        //String[] splits = authority.split("/");
        String baseUrl = absUrl.getProtocol() + "://" + authority;

        return baseUrl;
    }
}
