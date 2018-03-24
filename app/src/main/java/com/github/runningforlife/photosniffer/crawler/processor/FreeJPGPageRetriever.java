package com.github.runningforlife.photosniffer.crawler.processor;

import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import us.codecraft.webmagic.Page;

/**
 * retriever to retrieve images from free jpg
 */

public class FreeJPGPageRetriever extends PageRetriever {
    private static final String TAG = FreeJPGPageRetriever.class.getSimpleName();

    private static final String CLASS_THUMBNAIL = "thumbnail";

    private static final String FREE_JPG_IMAGE_START = "http://en.freejpg.com.ar/asset/";
    private static final String FREE_JPG_HREF_START = "http://en.freejpg.com.ar/free/";

    FreeJPGPageRetriever(HashMap<String, Boolean> pageState) {
        super(pageState);
    }

    @Override
    public List<String> retrieveImages(Page page) {
        List<String> images = Collections.EMPTY_LIST;

        Document doc = page.getHtml().getDocument();
        Elements imgElements = doc.getElementsByClass(CLASS_THUMBNAIL);
        if (imgElements != null && imgElements.size() > 0) {
            images = new ArrayList<>(imgElements.size());

            for (Element element : imgElements) {
                Elements imgTags = element.getElementsByTag(TAG_IMG);
                for (Element img : imgTags) {
                    String url = img.attr(ATTR_IMAGE_URL);
                    Log.d(TAG, "retrieve image url=" + url);
                    if (url != null && url.startsWith(FREE_JPG_IMAGE_START) && !images.contains(url)) {
                        images.add(url);
                    }
                }
            }
        }

        return images;
    }

    @Override
    public List<String> retrieveLinks(Page page) {
        List<String> links = Collections.EMPTY_LIST;

        Document doc = page.getHtml().getDocument();

        Elements elements = doc.getElementsByTag(TAG_HREF);
        if (elements != null && elements.size() > 0) {
            for (Element element : elements) {
                String pageUrl = element.attr(ATTR_REF);
                if (pageUrl != null && pageUrl.startsWith(FREE_JPG_HREF_START)
                        && !isPageRetrieved(pageUrl)) {
                    links.add(pageUrl);

                    page.addTargetRequest(pageUrl);
                }
            }
        }

        return links;
    }
}
