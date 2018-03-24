package com.github.runningforlife.photosniffer.crawler.processor;

import android.util.Log;
import android.webkit.URLUtil;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import us.codecraft.webmagic.Page;

/**
 * default pages retriever
 */

public class DefaultPageRetriever extends PageRetriever {
    private static final String TAG = DefaultPageRetriever.class.getSimpleName();

    DefaultPageRetriever(HashMap<String,Boolean> pageState) {
        super(pageState);
    }

    @Override
    public List<String> retrieveImages(Page page) {
        Document doc = page.getHtml().getDocument();
        Elements images = doc.select(REG_IMAGE);

        List<String> imgList = Collections.EMPTY_LIST;
        if (images != null && images.size() > 0) {
            imgList = new ArrayList<>();
            for (Element img : images) {
                // here, absolute URL and relative URL
                if (img.tagName().equals(TAG_IMG)) {
                    String url = img.attr(ATTR_IMAGE_URL);

                    Log.d(TAG, "retrieved image url = " + url);
                    if (!imgList.contains(url)) {
                        imgList.add(url);
                    }
                }
            }
        }

        return imgList;
    }

    @Override
    public List<String> retrieveLinks(Page page) {

        List<String> pageLinks = page.getHtml().links().all();

        List<String> pages = Collections.emptyList();
        if (pageLinks != null && pageLinks.size() > 0) {

            pages = new ArrayList<>();

            for (String link : pageLinks) {
                if (URLUtil.isValidUrl(link) && !isPageRetrieved(link)) {
                    page.addTargetRequest(link);

                    pages.add(link);
                }
            }
        }

        return pages;
    }
}
