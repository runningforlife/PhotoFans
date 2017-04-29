package jason.github.com.photofans.crawler.processor;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.utils.UrlUtil;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;

/**
 * Created by jason on 4/1/17.
 */

public class ImageRetrieverFactory implements ImageRetriever,ImageSource{
    private static final String TAG = "RetrieverFactory";

    private static Map<String,String> sImgSource = new HashMap<>();
    private static Map<String,String> sImageUrlStart = new HashMap<>();

    private static class InstanceHolder{
        private static final ImageRetrieverFactory instance = new ImageRetrieverFactory();
    }

    private ImageRetrieverFactory(){
        sImgSource.put(URL_FREE_JPG,REG_FREE_JPG);
        sImgSource.put(URL_ALBUM,REG_ALBUM);
        sImgSource.put(URL_PIXELS,REG_PIXELS);
        sImgSource.put(URL_PIXBABY,REG_PEXBABY);
        sImgSource.put(URL_PUBLIC_ARCHIVE,REG_PUBLIC_ARCHIVE);
        sImgSource.put(URL_VISUAL_CHINA,REG_VISUAL_CHINA);
        sImgSource.put(URL_VISUAL_HUNT,REG_VISUAL_HUNG);
        sImgSource.put(URL_YOUWU,REG_YOUWU);
        sImgSource.put(URL_MM,REG_MM);

        sImageUrlStart.put(URL_ALBUM,ALBUM_IMAGE_START);
        sImageUrlStart.put(URL_PIXBABY,PIXABAY_IMAGE_START);
        sImageUrlStart.put(URL_PUBLIC_ARCHIVE,PDN_IMAGE_START);
        sImageUrlStart.put(URL_VISUAL_CHINA,VC_IMAGE_START);
        sImageUrlStart.put(URL_PIXELS, PIXELS_IMAGE_START);
        sImageUrlStart.put(URL_YOUWU,YW_IMAGE_START);
        sImageUrlStart.put(URL_MM,MM_IMAGE_START);
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
                case URL_YOUWU:
                    imgList = retrieveMmImage(page,URL_YOUWU);
                    break;
                case URL_MM:
                    imgList = retrieveMmImage(page,URL_MM);
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
                // check the image url
                if(!URLUtil.isValidUrl(url) || !url.startsWith(imgSrc)
                        || checkImageUrlStart(url,imgSrc)){
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

    // check whether the url start with a given URL string
    private boolean checkImageUrlStart(String url, @IMAGE_SOURCE String src){
        Iterator it = sImageUrlStart.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            String rootUrl = (String)entry.getKey();
            String startUrl = (String)entry.getValue();
            if(src.equals(rootUrl) && url.startsWith(startUrl)){
                return true;
            }
        }

        return false;
    }

    private List<ImageRealm> retrieveMmImage(Page page, @IMAGE_SOURCE String src){
        Log.v(TAG,"retrieveMmImage(): url = " + page.getUrl());
        List<ImageRealm> imgUrls = new ArrayList<>();

        Html html =  page.getHtml();
        Document doc = html.getDocument();

        Log.v(TAG,"document = " + doc);
        Elements divs = null;
        String reg = null; // regression to retrieve images
        if(src.equals(URL_MM)) {
            // div whose class name is content
            divs = doc.select("div.content");
            reg = REG_MM;
        }else if(src.equals(URL_YOUWU)){
            // div whose class name is big-pic
            divs = doc.select("div.big-pic");
            reg = REG_YOUWU;
        }

        if(divs != null) {
            for (Element div : divs) {
                Elements allImgs = div.select(reg);
                for (Element img : allImgs) {
                    ImageRealm imageRealm = new ImageRealm();

                    String relUrl = img.attr("src");
                    String absUrl = img.attr("abs:src");
                    String url = absUrl;
                    // compose relative url and base url
                    if (!URLUtil.isValidUrl(absUrl)) {
                        url = src + relUrl;
                    }

                    String name = img.attr("alt");
                    if (TextUtils.isEmpty(name)) {
                        name = "unknown";
                    }
                    imageRealm.setName(name);
                    imageRealm.setUrl(url);
                    imageRealm.setTimeStamp(System.currentTimeMillis());
                    imageRealm.setUsed(false);

                    imgUrls.add(imageRealm);
                }
            }
        }
        Log.v(TAG,"retrieveMmImage(): retrieved images = " + imgUrls.size());
        return imgUrls;
    }
}
