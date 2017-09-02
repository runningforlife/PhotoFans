package com.github.runningforlife.photosniffer.crawler.processor;

import android.text.TextUtils;
import android.util.Log;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.model.QuotePageInfo;
import com.github.runningforlife.photosniffer.model.QuoteRealm;
import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.service.MyThreadFactory;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import us.codecraft.webmagic.Page;

/**
 * retrieve quotes from pages
 */

public class QuotesRetriever implements PageRetriever<QuoteRealm>{
    private static final String TAG = "QuotesRetriever";

    private static final String CLASS_CONTENT = "grid__content";
    private static final String CLASS_AUTHOR = "quote-single__author-name-link";
    private static final String CLASS_CONTENT_LINK = "grid__content-link";
    private static final String ATTR_LINK = "href";

    private int expect;
    private RetrieveCompleteListener listener;
    // for test
    private List<QuotePageInfo> pageList;
    private List<QuoteRealm> quoteList;

    public QuotesRetriever(int expect){
        this.expect = expect;

        pageList = new ArrayList<>();
        quoteList = new ArrayList<>();
    }

    public interface RetrieveCompleteListener{
        void onRetrieveComplete(int cnt);
    }

    public void setCompleteListener(RetrieveCompleteListener listener){
        this.listener = listener;
    }

    @Override
    public List<QuoteRealm> retrieve(Page page) {
        if(page == null) return null;

        Log.v(TAG,"retrieve()");

        Document doc = page.getHtml().getDocument();

        Elements elements = doc.getElementsByClass(CLASS_CONTENT);

        List<QuoteRealm> quotes = new ArrayList<>();
        // page list
        List<QuotePageInfo> quotePageList = new ArrayList<>();
        for(Element element : elements){
            Elements eAuthor = element.getElementsByClass(CLASS_AUTHOR);
            String author = eAuthor.get(0).ownText();
            // author pag list
            String pageUrl = eAuthor.attr(ATTR_LINK);
            QuotePageInfo pageInfo = new QuotePageInfo(pageUrl, false);
            quotePageList.add(pageInfo);
            // add requests
            page.addTargetRequest(pageUrl);

            Elements eContent = element.getElementsByClass(CLASS_CONTENT_LINK);
            String url = eContent.get(0).attr(ATTR_LINK);
            String text = eContent.text();
            if(!TextUtils.isEmpty(text)) {
                QuoteRealm quote = new QuoteRealm(url, author, text);
                quotes.add(quote);
            }
        }

        pageList.addAll(quotePageList);
        quoteList.addAll(quotes);
        // save quotes
        saveQuoteList(quotes);
        // save page list
        savePageList(quotePageList);

        if(quoteList.size() >= expect && listener != null){
            listener.onRetrieveComplete(quotes.size());
        }

        return quotes;
    }

    public List<QuotePageInfo> getPageList(){
        return pageList;
    }

    public List<QuoteRealm> getQuoteList(){
        return quoteList;
    }

    private void savePageList(final List<QuotePageInfo> pages){
        if(pages.size() <= 0) return;

        Log.v(TAG,"savePageList(): page size = " + pages.size());
        MyThreadFactory.getInstance().newThread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG,"savePageList()");
                RealmManager realmManager = RealmManager.getInstance();
                realmManager.saveQuotePage(pages);
            }
        }).start();
    }

    private void saveQuoteList(final List<QuoteRealm> quotes){
        if(quotes.size() <= 0) return;

        Log.v(TAG,"savePageList(): quotes size = " + quotes.size());
        MyThreadFactory.getInstance().newThread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG,"saveQuoteList()");
                RealmManager realmManager = RealmManager.getInstance();
                realmManager.saveQuoteRealm(quotes);
            }
        }).start();
    }
}
