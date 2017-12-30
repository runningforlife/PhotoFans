package com.github.runningforlife.photosniffer.crawler.processor;

import android.util.Log;
import android.webkit.URLUtil;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingDeque;

import com.github.runningforlife.photosniffer.utils.UrlUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class ImageRetrievePageProcessor implements PageProcessor {

    private static final String TAG = "ImagePageProcessor";

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);
    private HashMap<String,Boolean> mPagesState;
    // last url to start this page retrieving
    private List<String> mLastUrl;
    private int mCurrentImages;
    @SuppressWarnings("unchecked")
    private ImagePageFilter mPageFilter;

    private ImageRetrieverFactory mRetrieverFactory;
    private PageRetriever mPixelsRetriever;
    private BlockingDeque<List<String>> mDataQueue;

    public ImageRetrievePageProcessor(BlockingDeque<List<String>> dataQue, List<String> startUrl,
                                      HashMap<String,Boolean> pageState, PageFilter pageFilter) {
        mLastUrl = startUrl;
        mCurrentImages = 0;

        mDataQueue = dataQue;
        mPagesState = pageState;
        mPageFilter = (ImagePageFilter) pageFilter;

        //loadPages();

        mRetrieverFactory = ImageRetrieverFactory.getInstance();
        mPixelsRetriever = new PixelPageRetriever(mPagesState);
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

    public List<String> getStartUrl() {
        Log.d(TAG,"getStartUrl(): url = " + mLastUrl.size());
        return mLastUrl;
    }

    public int getRetrievedImageCount() {
        return mCurrentImages;
    }

    private void retrieveImages(Page page) {
        List<String> result = null;
        if (!isVisited(page) && isValidPage(page)) {
            try {
                if (page.getUrl().get().contains("https://www.pexels.com")) {
                    result = mPixelsRetriever.retrieveImages(page);
                    // save pages we get
                    List<String> pages = mPixelsRetriever.retrieveLinks(page);
                    mPagesState.put(page.getUrl().get(), true);
                    mDataQueue.put(pages);
                } else {
                    result = mRetrieverFactory.retrieveImages(page);
                    mPagesState.put(page.getUrl().get(), true);
                    // save to disk
                    mDataQueue.put(getPageList(page));
                }

                if (result != null) {
                    mDataQueue.put(result);
                }
            } catch (InterruptedException e) {
                Log.e(TAG,"fail to put data to queue");
            }
        }
    }

    private boolean isVisited(Page page) {
        return (mPagesState.containsKey(page.getUrl().get())
                && mPagesState.get(page.getUrl().get()));
    }

    private boolean isValidPage(Page page) {
        try {
            String baseUrl = UrlUtil.getRootUrl(page.getUrl().get());
            return URLUtil.isValidUrl(baseUrl) && mPageFilter.accept(baseUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isValidPageUrl(String url) {
        return URLUtil.isValidUrl(url);
    }

    private List<String> getPageList(Page page) {
        List<String> urlList = page.getHtml().links().all();
        List<String> pageList = new ArrayList<>(urlList.size() + 1);

        page.addTargetRequests(urlList);

        urlList.add(page.getUrl().get());
        for (String url : urlList) {
            if(!mPagesState.containsKey(url) && isValidPageUrl(url)) {
                pageList.add(url);
                mPagesState.put(url,urlList.indexOf(url) != -1);
            }
        }

        return pageList;
    }
}
