package com.github.runningforlife.photosniffer.crawler;

/**
 * Created by jason on 1/18/18.
 */

public class HighResImageUrlBuilder {

    private static final String PEXELS = "pexels";
    private static final String FREE_JPG = "freejpg";
    private static final String PIXABAY = "pixabay";
    private static final String VISUAL_HUNT = "visualhunt";

    public static String buildHighResImageUrl(String baseUrl) {
        if (baseUrl.contains(PEXELS)) {
            return buildHighResPixelsUrl(baseUrl, 650);
        } else if (baseUrl.contains(FREE_JPG)) {
            return buildHighResFreeJpgUrl(baseUrl);
        } else if (baseUrl.contains(PIXABAY)) {
            return buildPixabayHighResUrl(baseUrl);
        } else if (baseUrl.contains(VISUAL_HUNT)) {
            return buildVisualHuntHighResUrl(baseUrl);
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

    private static String buildPixabayHighResUrl(String baseUrl) {
        String highRes = "_960_720.jpg";

        int idx = baseUrl.indexOf("_");
        String subStr = baseUrl.substring(0, idx);

        return subStr + highRes;
    }

    private static String buildVisualHuntHighResUrl(String baseUrl) {
        String highRes = "l";

        int idx = baseUrl.indexOf("photos") + "photos".length();

        String sPart = baseUrl.substring(0, idx + 1) + highRes;

        return sPart + baseUrl.substring(idx + 2, baseUrl.length());
    }
}
