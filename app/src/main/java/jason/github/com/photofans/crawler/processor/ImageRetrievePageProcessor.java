package jason.github.com.photofans.crawler.processor;

import android.text.TextUtils;
import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import jason.github.com.photofans.model.ImageItem;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.repository.RealmHelper;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class ImageRetrievePageProcessor implements PageProcessor {

    private static final String TAG = "PageProcessor";

    // maximum number of letters of the name
    private final static int MAX_NAME_LEN = 10;

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

    private final static String URL_FREE_JPG = "http://en.freejpg.com.ar/free/images/";
    private final static String MATCH_FREE_JPG = "http://en.freejpg.com.ar/asset/";

    private final static String URL_PIXELS = "https://www.pexels.com/";
    private final static String URL_ALBUM = "http://albumarium.com/";

    private final static String REG_FREE_JPG = "http://en\\.freejpg\\.com\\.ar/.*(\\.(gif|jpg|png))$";

    private List<ImageItem> imgList = new ArrayList<ImageItem>();
    private List<RetrieveCompleteListener> mListeners;
    private int mMaxRetrievedImages = DEFAULT_RETRIEVED_IMAGES;

    private static final int DEFAULT_RETRIEVED_IMAGES = 20;

    public ImageRetrievePageProcessor(int n){
        mMaxRetrievedImages = n > 0 ? n : DEFAULT_RETRIEVED_IMAGES;
        mListeners = new ArrayList<>();
    }

    @Override
    public void process(Page page) {
        Log.v(TAG,"process()");

        retrieveImages(page);
        // notify jobs are done
        for(RetrieveCompleteListener listener : mListeners){
            listener.onRetrieveComplete(imgList);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public interface RetrieveCompleteListener{
        void onRetrieveComplete(List<ImageItem> data);
    }

    public void addListener(RetrieveCompleteListener listener){
        mListeners.add(listener);
    }

    public void removeListener(RetrieveCompleteListener listener){
        mListeners.remove(listener);
    }

    private void retrieveImages(Page page){

        while(imgList.size() <= mMaxRetrievedImages) {
            page.addTargetRequests(page.getHtml().links().regex(URL_FREE_JPG + ".*\\w").all());

            // here we retrieve all those IMAGE urls
            Document doc = page.getHtml().getDocument();
            Elements images = doc.select("img[src$=.jpg]");
            Log.v(TAG,"retrieved image size = " + images.size());
            for (Element img : images) {
                if (img.tagName().equals("img")) {
                    String url = img.attr("abs:src");
                    if(!url.startsWith(MATCH_FREE_JPG)){
                        continue;
                    }
                    Log.v(TAG,"retrieved image url = " + url);
                    ImageItem info = new ImageItem();


                    String imgName = img.attr("alt");
                    if(TextUtils.isEmpty(imgName)){
                        info.setName("unknown");
                    }else{
                        String str = imgName;
                        if(imgName.startsWith("free images")){
                            str = imgName.substring("free images".length());
                        }
                        String firstWord = str.contains(" ") ? str.substring(0,str.indexOf(" "))
                                : str;
                        info.setName(firstWord);
                    }
                    info.setUrl(url);
                    info.setTimeStamp(System.currentTimeMillis());

                    if(!imgList.contains(info)) {
                        imgList.add(info);
                    }
                    // enough already
                    if(imgList.size() > mMaxRetrievedImages){
                        break;
                    }
                }
            }
        }
    }

}
