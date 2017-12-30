package com.github.runningforlife.photosniffer.utils;

import android.webkit.URLUtil;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * an utility class to process URLs
 */

public class UrlUtil {

    private final static String REG_IMAGES = "(http)?s?:?(\\/\\/[^\"']*\\.(?:jpg|jpeg|gif|png|svg))";

    // get a root url of the give url
    public static String getRootUrl(String url) throws MalformedURLException{
        URL absUrl = new URL(url);
        String authority = absUrl.getAuthority();
        //String[] splits = authority.split("/");
        String baseUrl = absUrl.getProtocol() + "://" + authority;

        return baseUrl;
    }

    public static boolean isPossibleImageUrl(String url){
        Pattern pattern = Pattern.compile(REG_IMAGES);
        Matcher matcher;
        if (url.contains("?")) {
            matcher = pattern.matcher(url.substring(0, url.indexOf("?")));
        } else {
            matcher = pattern.matcher(url);
        }

        return matcher.matches();
    }
}
