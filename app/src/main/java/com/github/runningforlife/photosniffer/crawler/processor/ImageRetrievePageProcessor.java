package com.github.runningforlife.photosniffer.crawler.processor;

import android.os.Message;
import android.util.Log;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

import com.github.runningforlife.photosniffer.crawler.DataSaver;
import com.github.runningforlife.photosniffer.utils.UrlUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class ImageRetrievePageProcessor implements PageProcessor {

    private static final String TAG = "ImagePageProcessor";

    private static final String PAGE_PEXELS = "pexels";
    private static final String PAGE_FREE_JPG = "freejpg";
    private static final String PAGE_PIEXABAY = "pixabay";
    private static final String PAGE_VISUAL_HUNT = "visualhunt";
    private static final String PAGE_DEFAULT = "default";

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);
    private HashMap<String,Boolean> mPagesState;
    private int mCurrentImages;
    @SuppressWarnings("unchecked")
    private ImagePageFilter mPageFilter;
    private HashMap<String, PageRetriever> mRetrieversMap;
    private DataSaver mDataSaver;

    public ImageRetrievePageProcessor(DataSaver saver, List<String> startUrl, HashMap<String,Boolean> pageState, PageFilter pageFilter) {
        mCurrentImages = 0;

        mDataSaver = saver;
        mPagesState = pageState;
        mPageFilter = (ImagePageFilter) pageFilter;

        mRetrieversMap = new HashMap<>(startUrl.size());

        mRetrieversMap.put(PAGE_DEFAULT, new DefaultPageRetriever(mPagesState));
        for (String url : startUrl) {
            if (url.contains(PAGE_FREE_JPG)) {
                mRetrieversMap.put(PAGE_FREE_JPG, new FreeJPGPageRetriever(mPagesState));
            } else if (url.contains(PAGE_PEXELS)) {
                mRetrieversMap.put(PAGE_PEXELS, new PixelPageRetriever(mPagesState));
            } else if (url.contains(PAGE_PIEXABAY)) {
                mRetrieversMap.put(PAGE_PIEXABAY, new PixabayPageRetriever(mPagesState));
            } else if (url.contains(PAGE_VISUAL_HUNT)) {
                mRetrieversMap.put(PAGE_VISUAL_HUNT, new VisualHuntPageRetriever(mPagesState));
            }
        }
    }

    @Override
    public void process(Page page) {
        Log.v(TAG,"process()");

        if(page != null) {
            int status = page.getStatusCode();
            if (status == 200) {
                retrieveImages(page);
            } else if (status >= 400 && status <= 511) {
                // error happens
                Log.v(TAG,"cannot download page,status = " + status);
            }
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public synchronized void onDestroy() {
        if (mRetrieversMap != null) {
            mRetrieversMap.clear();
        }
    }

    public int getRetrievedImageCount() {
        return mCurrentImages;
    }

    private void retrieveImages(Page page) {
        List<String> result;
        List<String> pages;

        String pageUrl = page.getUrl().get();
        if (!isVisited(pageUrl) && isValidPage(pageUrl)) {

            PageRetriever pageRetriever = mRetrieversMap.get(PAGE_DEFAULT);
            if (pageUrl.contains(PAGE_PEXELS)) {
                pageRetriever = mRetrieversMap.get(PAGE_PEXELS);
            } else if (pageUrl.contains(PAGE_FREE_JPG)) {
                pageRetriever = mRetrieversMap.get(PAGE_FREE_JPG);
            } else if (pageUrl.contains(PAGE_PIEXABAY)) {
                pageRetriever = mRetrieversMap.get(PAGE_PIEXABAY);
            } else if (pageUrl.contains(PAGE_VISUAL_HUNT)) {
                pageRetriever = mRetrieversMap.get(PAGE_VISUAL_HUNT);
            }

            mPagesState.put(pageUrl, true);

            result = pageRetriever.retrieveImages(page);
            if (result != null && result.size() > 0) {
                mCurrentImages += result.size();
                //mDataQueue.put(result);
                Message message = mDataSaver.obtainMessage(DataSaver.REQUEST_IMAGE_SAVE);
                message.obj = result;
                message.sendToTarget();
            }

            pages = pageRetriever.retrieveLinks(page);
            if (pages.size() > 0) {
                //mDataQueue.put(pages);
                Message message = mDataSaver.obtainMessage(DataSaver.REQUEST_IMAGE_SAVE);
                message.obj = pages;
                message.sendToTarget();
            }
        }
    }

    private boolean isVisited(String pageUrl) {
        return (mPagesState.containsKey(pageUrl) && mPagesState.get(pageUrl));
    }

    private boolean isValidPage(String page) {
        try {
            String baseUrl = UrlUtil.getRootUrl(page);
            return mPageFilter.accept(baseUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
