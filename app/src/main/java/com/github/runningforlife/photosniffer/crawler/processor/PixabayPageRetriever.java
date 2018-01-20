package com.github.runningforlife.photosniffer.crawler.processor;

import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import us.codecraft.webmagic.Page;

/**
 * pixabay page retriever
 */

public class PixabayPageRetriever extends PageRetriever {
    private static final String TAG = PixabayPageRetriever.class.getSimpleName();

    private static final String CLASS_GALLERY = "flex_grid credits";
    private static final String CLASS_RELATED = "flex_grid related_photos";
    private static final String CLASS_ITEM = "item";
    private static final String IMAGE_START = "https://cdn.pixabay.com/photo/";


    PixabayPageRetriever(HashMap<String, Boolean> pageState) {
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
                if (url.startsWith(IMAGE_START) && !imgList.contains(url)) {
                    imgList.add(url);
                }
            }
        }

        return imgList;
    }

    @Override
    public List<String> retrieveLinks(Page page) {
        Log.d(TAG,"retrieveLinks()");
        List<String> links = page.getHtml().links().all();

        List<String> pageUrls = new ArrayList<>();
        for (String link : links) {
            if (!isPageRetrieved(link) && !pageUrls.contains(link) && link.contains("pixabay")) {
                pageUrls.add(link);
            }
        }

        return pageUrls;
    }
}
