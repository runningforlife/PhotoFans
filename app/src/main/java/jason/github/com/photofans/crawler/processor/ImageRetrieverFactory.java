package jason.github.com.photofans.crawler.processor;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.utils.UrlUtil;
import us.codecraft.webmagic.Page;

/**
 * Created by jason on 4/1/17.
 */

public class ImageRetrieverFactory implements ImageRetriever,ImageSource{
    private static final String TAG = "RetrieverFactory";

    private static Map<String,String> sImgSource = new HashMap<>();

    private static class InstanceHolder{
        private static final ImageRetrieverFactory instance = new ImageRetrieverFactory();
    }

    private ImageRetrieverFactory(){
        sImgSource.put(URL_FREE_JPG,REG_FREE_JPG);
        sImgSource.put(URL_ALBUM,REG_ALBUM);
        sImgSource.put(URL_1X,REG_1X);
        sImgSource.put(URL_ILLUSION,REG_ILLUSION);
        sImgSource.put(URL_PIXELS,REG_PIXELS);
        sImgSource.put(URL_PIXBABY,REG_PEXBABY);
        sImgSource.put(URL_PUBLIC_ARCHIVE,REG_PUBLIC_ARCHIVE);
        sImgSource.put(URL_VISUAL_CHINA,REG_VISUAL_CHINA);
        sImgSource.put(URL_VISUAL_HUNT,REG_VISUAL_HUNG);
    }

    public static ImageRetrieverFactory getInstance(){
        return InstanceHolder.instance;
    }

    @Override
    public List<ImageRealm> retrieveImages(Page page){
        if(page == null){
            throw new IllegalArgumentException("page should not be null");
        }
        List<ImageRealm> imgList = Collections.EMPTY_LIST;

        String url = page.getUrl().get();
        Log.v(TAG,"retrieved page base url = " + url);
        try {
            String baseUrl = UrlUtil.getRootUrl(url);
            switch (baseUrl){
                case URL_FREE_JPG:
                    imgList = retrieve(page,URL_FREE_JPG);
                    break;
                case URL_ALBUM:
                    imgList = retrieve(page,URL_ALBUM);
                    break;
                case URL_1X:
                    imgList = retrieve(page,URL_1X);
                    break;
                case URL_ILLUSION:
                    imgList = retrieve(page,URL_ILLUSION);
                    break;
                case URL_PIXBABY:
                    imgList = retrieve(page,URL_PIXBABY);
                    break;
                case URL_PUBLIC_ARCHIVE:
                    imgList = retrieve(page,URL_PUBLIC_ARCHIVE);
                    break;
                case URL_PIXELS:
                    imgList = retrieve(page,URL_PIXELS);
                    break;
                case URL_VISUAL_CHINA:
                    imgList = retrieve(page,URL_VISUAL_CHINA);
                    break;
                case URL_VISUAL_HUNT:
                    imgList = retrieve(page,URL_VISUAL_HUNT);
                    break;
                default:
                    break;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return imgList;
    }

    private List<ImageRealm> retrieve(Page page,@IMAGE_SOURCE String imgSrc){
        Log.v(TAG,"retrieve(): page url = " + page.getUrl().get());
        // here we retrieve all those IMAGE urls
        String retrieveReg = sImgSource.get(imgSrc);
        Document doc = page.getHtml().getDocument();
        Elements images = doc.select(retrieveReg);
        Log.v(TAG, "retrieved images = " + images.size());

        List<ImageRealm> imgList = new ArrayList<>();
        for (Element img : images) {
            // here, absolute URL and relative URL
            if (img.tagName().equals("img")) {
                String relUrl = img.attr("src");
                String absUrl = img.attr("abs:src");
                String url = absUrl;
                // compose relative url and base url
                if(!URLUtil.isValidUrl(absUrl)){
                    url = imgSrc + relUrl;
                }

                if(!URLUtil.isValidUrl(url) || !url.startsWith(imgSrc)){
                    continue;
                }

                Log.v(TAG, "retrieved image url = " + url);

                ImageRealm imageRealm = new ImageRealm();

                String imgName = img.attr("alt");
                if (TextUtils.isEmpty(imgName)) {
                    imageRealm.setName("unknown");
                }
                imageRealm.setName(imgName);
                imageRealm.setUrl(url);
                imageRealm.setTimeStamp(System.currentTimeMillis());
                imageRealm.setUsed(false);

                imgList.add(imageRealm);
            }
        }

        return imgList;
    }
}
