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
 * a class to retrieveImages images from https://www.pexels.com
 */

public class PixelPageRetriever extends PageRetriever {
    private static final String TAG = "PixelPageRetriever";

    static final int MIN_HEIGHT = 350;
    // class tag
    private static final String CLASS_PHOTO = "photos";
    private static final String CLASS_PHOTO_DETAIL = "photo-details";
    private static final String CLASS_MORE_PAGE = "pagination";

    private static final String PIXELS_IMAGE_START = "https://images.pexels.com/photos/";

    PixelPageRetriever(HashMap<String, Boolean> pageState) {
        super(pageState);
    }

    @Override
    public List<String> retrieveImages(Page page) {
        Log.v(TAG,"retrieveImages(): page url = " + page.getUrl().get());
        // 1. photos 2. photo-details
        Document doc = page.getHtml().getDocument();

        Elements images = new Elements(0);

        Elements photos = doc.getElementsByClass(CLASS_PHOTO);
        for (Element photo : photos) {
            images.addAll(photo.getElementsByTag(TAG_IMG));
        }

        Elements photoDetails = doc.getElementsByClass(CLASS_PHOTO_DETAIL);
        for (Element detail : photoDetails) {
            images.addAll(detail.getElementsByTag(TAG_IMG));
        }

        List<String> imgList = new ArrayList<>();

        for (Element img : images) {
            String url = img.attr(ATTR_IMAGE_URL);
            String imgName = img.attr(ATTR_ALT);
            //String height = img.attr(ATTR_HEIGHT);
            Log.d(TAG, "retrieved image url = " + url);
            if (url.startsWith(PIXELS_IMAGE_START)) {
                imgList.add(url);
            }
        }

        return imgList;
    }

    @Override
    public List<String> retrieveLinks(Page page) {
        Log.v(TAG,"retrieveLinks()");

        //mPageState.put(page.getUrl().get(), true);

        List<String> pageLinks = new ArrayList<>();
        // 2 kinds of url we need to retrieve
        // 1. url from tag img 2. url from ie-fallback(page)

        final Document doc = page.getHtml().getDocument();

        Elements links = new Elements(0);

        Elements tags = doc.getElementsByClass(CLASS_PHOTO);
        for (Element tag : tags) {
            links.addAll(tag.getElementsByTag("a"));
        }

        tags = doc.getElementsByClass(CLASS_PHOTO_DETAIL);
        for (Element tag : tags) {
            links.addAll(tag.getElementsByTag("a"));
        }

        tags = doc.getElementsByClass(CLASS_MORE_PAGE);
        Element morePage = tags.get(0);
        Elements refs = morePage.getElementsByTag("a");
        String refUrl = refs.get(0).attr(ATTR_REF);
        String baseUrl = refUrl.substring(0, refUrl.indexOf("=") + 1);
        // add all navigation pages url
        int pageNo = getLargestPageNo(morePage);
        for (int i = 2; i < pageNo; ++i) {
            String url = baseUrl + i;
            if (mPageState.size() == 0 || !mPageState.containsKey(url) || !mPageState.get(url)) {
                page.addTargetRequest(url);
                pageLinks.add(url);
            }
        }

        // get all links we have
        for (Element link : links) {
            if (link.hasAttr(ATTR_REF)) {
                String url = link.attr(ATTR_REF);
                if (mPageState.size() == 0 || !mPageState.containsKey(url) || !mPageState.get(url)) {
                    page.addTargetRequest(url);
                    pageLinks.add(url);
                }
            }
        }

        String url = page.getUrl().get();
        if (!pageLinks.contains(url)) {
            pageLinks.add(url);
        }

        return pageLinks;
    }

    private int getLargestPageNo(Element morePage) {
        Elements hrefs = morePage.getElementsByTag("a");
        // the last page no
        String pageNo = hrefs.get(hrefs.size() - 2).text();

        int num = 1;
        try {
            num = Integer.parseInt(pageNo);
        } catch (Exception e){
            e.printStackTrace();
        }

        return num;
    }
}
