package com.github.runningforlife.photosniffer.crawler;

/**
 * Created by jason on 1/18/18.
 */

public class HighResImageUrlBuilder {

    private static final String PEXELS = "pexels";
    private static final String FREE_JPG = "freejpg";

    public static String buildHighResImageUrl(String baseUrl) {
        if (baseUrl.contains(PEXELS)) {
            return buildHighResPixelsUrl(baseUrl, 650);
        } else if (baseUrl.contains(FREE_JPG)) {
            return buildHighResFreeJpgUrl(baseUrl);
        }

        return baseUrl;
    }

    private static String buildHighResPixelsUrl(String url, int res) {
        int hIdx = url.indexOf("?");
        return url.substring(0, hIdx)
                + "?"
                + "h=" + res
                + "&auto=compress"
                + "&cs=tinysrgb";
    }

    private static String buildHighResFreeJpgUrl(String url) {
        int idx = url.indexOf("asset");
        int eIdx = url.indexOf("/", idx + "asset".length() + 1);

        String sPart = url.substring(0, idx + "asset".length() + 1);
        String ePart = url.substring(eIdx, url.length());

        return sPart + String.valueOf(900) + ePart;
    }
}
