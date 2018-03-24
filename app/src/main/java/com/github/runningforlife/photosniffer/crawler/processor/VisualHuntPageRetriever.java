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
 * visual hunt page retriever
 */

public class VisualHuntPageRetriever extends PageRetriever {
    private static final String TAG = VisualHuntPageRetriever.class.getSimpleName();

    private static final String CLASS_GALLERY = "Collage vh-Collage-items vh-Collage-items--photos";
    private static final String CLASS_ITEM = "vh-Collage-item";
    private static final String IMAGE_START = "https://visualhunt.com/photos/";

    VisualHuntPageRetriever(HashMap<String, Boolean> pageState) {
        super(pageState);
    }

    @Override
    public List<String> retrieveImages(Page page) {
        Log.d(TAG,"retrieveImages()");
        Document doc = page.getHtml().getDocument();

        Elements elements = doc.getElementsByClass(CLASS_ITEM);

        List<String> imgList = new ArrayList<>();
        for (Element element : elements) {
            Elements images = element.getElementsByTag(TAG_IMG);

            for (Element img : images) {
                String url = img.attr(ATTR_IMAGE_URL);
                Log.d(TAG,"retrieveImages(): image url=" + url);
                if (url != null && url.startsWith(IMAGE_START) && !imgList.contains(url)) {
                    imgList.add(url);
                }
            }
        }

        return imgList;
    }

    @Override
    public List<String> retrieveLinks(Page page) {
        Log.d(TAG,"retrieveLinks()");
        List<String> links  = page.getHtml().links().all();

        List<String> pageLinks = Collections.EMPTY_LIST;
        if (links != null && links.size() > 0) {
            pageLinks = new ArrayList<>();
            for (String link : links) {
                if (link != null && !isPageRetrieved(link) &&
                        !pageLinks.contains(link) && link.contains("visualhunt")) {
                    pageLinks.add(link);
                }
            }
        }

        return pageLinks;
    }


}
