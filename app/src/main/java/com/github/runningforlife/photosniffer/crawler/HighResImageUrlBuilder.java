package com.github.runningforlife.photosniffer.crawler;

import com.github.runningforlife.photosniffer.crawler.processor.ImageSource;

/**
 * Created by jason on 1/18/18.
 */

public class HighResImageUrlBuilder {

    private static final String PEXELS = "pexels";
    private static final String FREE_JPG = "freejpg";
    private static final String PIXABAY = "pixabay";
    private static final String VISUAL_HUNT = "visualhunt";

    public static String buildHighResImageUrl(String baseUrl, boolean  isExtraHigh) {
        if (baseUrl.contains(PEXELS)) {
            return buildHighResPixelsUrl(baseUrl, isExtraHigh);
        } else if (baseUrl.contains(FREE_JPG)) {
            return buildHighResFreeJpgUrl(baseUrl, isExtraHigh);
        } else if (baseUrl.contains(PIXABAY)) {
            return buildPixabayHighResUrl(baseUrl, isExtraHigh);
        } else if (baseUrl.contains(VISUAL_HUNT)) {
            return buildVisualHuntHighResUrl(baseUrl, isExtraHigh);
        }

        return baseUrl;
    }

    private static String buildHighResPixelsUrl(String url, boolean isExtraHigh) {
        int hIdx = url.indexOf("?");

        final int defaultHeight = 650;
        final int defaultWidth = 940;

        final int extraHeight = 750;
        final int extraWidth = 1260;

        if (isExtraHigh) {
            return url.substring(0, hIdx)
                    + "?"
                    + "h=" + extraHeight
                    + "&w=" + extraWidth
                    + "&auto=compress"
                    + "&cs=tinysrgb";
        } else {
            return url.substring(0, hIdx)
                    + "?"
                    + "h=" + defaultHeight
                    + "&w=" + defaultWidth
                    + "&auto=compress"
                    + "&cs=tinysrgb";
        }
    }

    private static String buildHighResFreeJpgUrl(String url, boolean isExtraHigh) {
        int idx = url.indexOf("asset");
        int eIdx = url.indexOf("/", idx + "asset".length() + 1);
        int noIdxStart = url.indexOf("F");
        int noIdexEnd = url.indexOf(".");

        String sPart = url.substring(0, idx + "asset".length() + 1);
        String ePart = url.substring(eIdx, url.length());

        String noPart = url.substring(noIdxStart, noIdexEnd);

        return isExtraHigh ? (ImageSource.URL_FREEJPG_EXTRA + noPart) : (sPart + String.valueOf(900) + ePart);
    }

    private static String buildPixabayHighResUrl(String baseUrl, boolean isExtraHigh) {
        String highRes = "_960_720.jpg";
        String extraHighRes = "_1280.jpg";

        int idx = baseUrl.indexOf("_");
        String subStr = baseUrl.substring(0, idx);

        return isExtraHigh ? (subStr + extraHighRes) : (subStr + highRes);
    }

    private static String buildVisualHuntHighResUrl(String baseUrl, boolean isExtraHigh) {
        String highRes = "xl";
        String extraHigh = "xl2";

        int idx = baseUrl.indexOf("photos") + "photos".length();

        String sPart = baseUrl.substring(0, idx + 1) + highRes;
        String extra_sPart = baseUrl.substring(0, idx + 1) + extraHigh;

        return isExtraHigh ? (extra_sPart + baseUrl.substring(idx + 2, baseUrl.length())) :
                (sPart + baseUrl.substring(idx + 2, baseUrl.length()));
    }
}
